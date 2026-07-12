package br.uel.gacs.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Centraliza a conversão e a apresentação dos dados numéricos do GACS. */
public final class FormatadorNumero {
    public static final int CASAS_DECIMAIS = 6;

    private static final String PADRAO_CIENTIFICO = "0.000000E00";

    private FormatadorNumero() { }

    /**
     * Converte uma entrada numérica, aceitando ponto ou vírgula como separador
     * decimal e notação científica com {@code E} maiúsculo ou minúsculo.
     */
    public static Double converter(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O valor numérico não pode estar vazio.");
        }

        String normalizado = texto.trim().replace(',', '.');
        try {
            double valor = Double.parseDouble(normalizado);
            validarFinito(valor);
            return valor;
        } catch (NumberFormatException excecao) {
            throw new IllegalArgumentException("O valor informado não é um número válido.", excecao);
        }
    }

    /**
     * Formata o valor em notação científica com seis casas decimais, vírgula
     * decimal e sinal explícito no expoente, sem modificar o valor armazenado.
     */
    public static String formatar(Double valor) {
        if (valor == null) {
            return "";
        }
        validarFinito(valor);

        DecimalFormat formato = novoFormato();
        String resultado = formato.format(valor);
        int posicaoExpoente = resultado.indexOf('E');
        if (posicaoExpoente >= 0 && posicaoExpoente + 1 < resultado.length()) {
            char primeiroCaractereExpoente = resultado.charAt(posicaoExpoente + 1);
            if (primeiroCaractereExpoente != '-' && primeiroCaractereExpoente != '+') {
                resultado = resultado.substring(0, posicaoExpoente + 1) + "+"
                        + resultado.substring(posicaoExpoente + 1);
            }
        }
        return resultado;
    }

    private static DecimalFormat novoFormato() {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR"));
        simbolos.setDecimalSeparator(',');
        simbolos.setExponentSeparator("E");

        DecimalFormat formato = new DecimalFormat(PADRAO_CIENTIFICO, simbolos);
        formato.setGroupingUsed(false);
        return formato;
    }

    private static void validarFinito(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("O valor numérico deve ser finito.");
        }
    }
}
