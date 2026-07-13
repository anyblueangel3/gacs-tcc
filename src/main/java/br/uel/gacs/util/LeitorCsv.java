package br.uel.gacs.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lê arquivos CSV UTF-8, incluindo campos entre aspas e aspas duplicadas. */
public final class LeitorCsv {
    private LeitorCsv() { }

    public static List<List<String>> ler(Path arquivo) throws IOException {
        String texto = Files.readString(arquivo, StandardCharsets.UTF_8);
        if (texto.startsWith("\uFEFF")) texto = texto.substring(1);
        char separador = detectarSeparador(texto);
        List<List<String>> linhas = analisar(texto, separador);
        while (!linhas.isEmpty() && linhas.getLast().stream().allMatch(String::isBlank)) linhas.removeLast();
        return List.copyOf(linhas);
    }

    private static char detectarSeparador(String texto) {
        String primeira = texto.lines().findFirst().orElse("");
        int virgulas = contarForaDeAspas(primeira, ',');
        int pontoEVirgulas = contarForaDeAspas(primeira, ';');
        // CSV exportado pelo Excel em regiões com vírgula decimal normalmente
        // usa ponto e vírgula entre campos.
        return pontoEVirgulas > 0 ? ';' : ',';
    }

    private static int contarForaDeAspas(String linha, char procurado) {
        boolean aspas=false; int total=0;
        for(int i=0;i<linha.length();i++){
            char c=linha.charAt(i);
            if(c=='"') { if(aspas&&i+1<linha.length()&&linha.charAt(i+1)=='"')i++; else aspas=!aspas; }
            else if(!aspas&&c==procurado)total++;
        }
        return total;
    }

    private static List<List<String>> analisar(String texto, char separador) {
        List<List<String>> linhas=new ArrayList<>(); List<String> linha=new ArrayList<>();
        StringBuilder campo=new StringBuilder(); boolean aspas=false;
        for(int i=0;i<texto.length();i++){
            char c=texto.charAt(i);
            if(c=='"') { if(aspas&&i+1<texto.length()&&texto.charAt(i+1)=='"'){campo.append('"');i++;}else aspas=!aspas; }
            else if(!aspas&&c==separador){linha.add(campo.toString());campo.setLength(0);}
            else if(!aspas&&(c=='\n'||c=='\r')){
                linha.add(campo.toString());campo.setLength(0);linhas.add(List.copyOf(linha));linha.clear();
                if(c=='\r'&&i+1<texto.length()&&texto.charAt(i+1)=='\n')i++;
            } else campo.append(c);
        }
        if(aspas)throw new IllegalArgumentException("O arquivo CSV possui um campo com aspas não encerradas.");
        if(campo.length()>0||!linha.isEmpty()){linha.add(campo.toString());linhas.add(List.copyOf(linha));}
        return linhas;
    }
}
