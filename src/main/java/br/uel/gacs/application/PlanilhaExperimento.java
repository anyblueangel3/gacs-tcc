package br.uel.gacs.application;

import br.uel.gacs.util.FormatadorNumero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mantém em memória os nomes das colunas e os valores numéricos apresentados
 * na grade de um experimento, independentemente da forma de entrada dos dados.
 */
public final class PlanilhaExperimento {
    public static final int MAXIMO_COLUNAS = 50;
    public static final int MAXIMO_MEDIDAS = 10_000;

    private final List<String> nomesColunas = new ArrayList<>();
    private final List<List<Double>> medidas = new ArrayList<>();

    /** Cria uma planilha vazia para entrada por digitação. */
    public PlanilhaExperimento() { }

    /**
     * Constrói a planilha a partir de células obtidas por colagem ou importação.
     * A primeira linha é tratada como cabeçalho quando contém ao menos uma
     * célula não numérica. Todas as células das demais linhas são obrigatórias
     * e devem representar números válidos.
     */
    public static PlanilhaExperimento deCelulas(List<? extends List<String>> celulas) {
        validarMatriz(celulas);

        PlanilhaExperimento planilha = new PlanilhaExperimento();
        int quantidadeColunas = celulas.getFirst().size();
        boolean possuiCabecalho = possuiCabecalho(celulas.getFirst());

        for (int coluna = 0; coluna < quantidadeColunas; coluna++) {
            String nome = possuiCabecalho
                    ? validarNomeColuna(celulas.getFirst().get(coluna), coluna)
                    : "";
            planilha.adicionarColuna(nome);
        }

        int primeiraMedida = possuiCabecalho ? 1 : 0;
        for (int linha = primeiraMedida; linha < celulas.size(); linha++) {
            List<Double> valores = new ArrayList<>(quantidadeColunas);
            for (int coluna = 0; coluna < quantidadeColunas; coluna++) {
                valores.add(converterNumero(celulas.get(linha).get(coluna), linha, coluna));
            }
            planilha.adicionarMedida(valores);
        }

        return planilha;
    }

    public int getQuantidadeColunas() {
        return nomesColunas.size();
    }

    public int getQuantidadeMedidas() {
        return medidas.size();
    }

    public List<String> getNomesColunas() {
        return Collections.unmodifiableList(nomesColunas);
    }

    public String getNomeColuna(int coluna) {
        validarIndiceColuna(coluna);
        return nomesColunas.get(coluna);
    }

    /** Retorna o rótulo posicional da coluna segundo a convenção A, ..., Z, AA, ..., AX. */
    public String getRotuloColuna(int coluna) {
        validarIndiceColuna(coluna);
        return rotuloColuna(coluna);
    }

    public void setNomeColuna(int coluna, String nome) {
        validarIndiceColuna(coluna);
        nomesColunas.set(coluna, validarNomeColuna(nome, coluna));
    }

    public void adicionarColuna(String nome) {
        if (nomesColunas.size() >= MAXIMO_COLUNAS) {
            throw new IllegalArgumentException("A planilha admite no máximo 50 colunas.");
        }
        nomesColunas.add(validarNomeColuna(nome, nomesColunas.size()));
        for (List<Double> linha : medidas) {
            linha.add(null);
        }
    }

    /** Adiciona uma linha vazia para posterior preenchimento por digitação. */
    public void adicionarMedidaVazia() {
        exigirColunas();
        exigirEspacoParaMedida();
        medidas.add(new ArrayList<>(Collections.nCopies(nomesColunas.size(), null)));
    }

    /** Adiciona uma linha numérica completa, usada por colagem ou importação. */
    public void adicionarMedida(List<Double> valores) {
        exigirColunas();
        exigirEspacoParaMedida();
        if (valores == null || valores.size() != nomesColunas.size()) {
            throw new IllegalArgumentException("A medida deve possuir um valor para cada coluna.");
        }
        if (valores.stream().anyMatch(valor -> valor == null || !Double.isFinite(valor))) {
            throw new IllegalArgumentException("Todos os valores da medida devem ser números finitos.");
        }
        medidas.add(new ArrayList<>(valores));
    }

    public Double getValor(int linha, int coluna) {
        validarCelula(linha, coluna);
        return medidas.get(linha).get(coluna);
    }

    /** Define um valor digitado; {@code null} representa uma célula ainda vazia. */
    public void setValor(int linha, int coluna, Double valor) {
        validarCelula(linha, coluna);
        if (valor != null && !Double.isFinite(valor)) {
            throw new IllegalArgumentException("O valor da célula deve ser um número finito.");
        }
        medidas.get(linha).set(coluna, valor);
    }

    public List<Double> getMedida(int linha) {
        if (linha < 0 || linha >= medidas.size()) {
            throw new IndexOutOfBoundsException("Linha inexistente: " + linha);
        }
        return Collections.unmodifiableList(medidas.get(linha));
    }

    public void removerMedida(int linha) {
        if (linha < 0 || linha >= medidas.size()) {
            throw new IndexOutOfBoundsException("Linha inexistente: " + linha);
        }
        medidas.remove(linha);
    }

    /** Indica se todas as células existentes estão preenchidas com números. */
    public boolean estaCompleta() {
        return medidas.stream().flatMap(List::stream).allMatch(valor -> valor != null);
    }

    private static void validarMatriz(List<? extends List<String>> celulas) {
        if (celulas == null || celulas.isEmpty() || celulas.getFirst() == null
                || celulas.getFirst().isEmpty()) {
            throw new IllegalArgumentException("Não foram encontrados dados para a planilha.");
        }
        int colunas = celulas.getFirst().size();
        if (colunas > MAXIMO_COLUNAS) {
            throw new IllegalArgumentException("A planilha admite no máximo 50 colunas.");
        }
        if (celulas.size() > MAXIMO_MEDIDAS + 1) {
            throw new IllegalArgumentException("A planilha admite no máximo 10.000 medidas.");
        }
        for (int linha = 0; linha < celulas.size(); linha++) {
            List<String> atual = celulas.get(linha);
            if (atual == null || atual.size() != colunas) {
                throw new IllegalArgumentException(
                        "A linha " + (linha + 1) + " não possui a quantidade esperada de colunas.");
            }
        }
    }

    private static boolean possuiCabecalho(List<String> primeiraLinha) {
        for (String celula : primeiraLinha) {
            try {
                converterNumero(celula, 0, 0);
            } catch (IllegalArgumentException excecao) {
                return true;
            }
        }
        return false;
    }

    private static Double converterNumero(String texto, int linha, int coluna) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("A célula da linha " + (linha + 1)
                    + ", coluna " + rotuloColuna(coluna) + " está vazia.");
        }
        try {
            return FormatadorNumero.converter(texto);
        } catch (IllegalArgumentException excecao) {
            throw new IllegalArgumentException("O valor da linha " + (linha + 1)
                    + ", coluna " + rotuloColuna(coluna) + " não é numérico.");
        }
    }

    private static String validarNomeColuna(String nome, int coluna) {
        if (nome == null || nome.isBlank()) {
            return "";
        }
        String nomeTratado = nome.trim();
        if (nomeTratado.length() > 200) {
            throw new IllegalArgumentException("O nome da coluna " + rotuloColuna(coluna)
                    + " deve possuir no máximo 200 caracteres.");
        }
        return nomeTratado;
    }

    private void exigirColunas() {
        if (nomesColunas.isEmpty()) {
            throw new IllegalStateException("Adicione ao menos uma coluna antes das medidas.");
        }
    }

    private void exigirEspacoParaMedida() {
        if (medidas.size() >= MAXIMO_MEDIDAS) {
            throw new IllegalArgumentException("A planilha admite no máximo 10.000 medidas.");
        }
    }

    private void validarIndiceColuna(int coluna) {
        if (coluna < 0 || coluna >= nomesColunas.size()) {
            throw new IndexOutOfBoundsException("Coluna inexistente: " + coluna);
        }
    }

    private void validarCelula(int linha, int coluna) {
        if (linha < 0 || linha >= medidas.size()) {
            throw new IndexOutOfBoundsException("Linha inexistente: " + linha);
        }
        validarIndiceColuna(coluna);
    }

    private static String rotuloColuna(int indice) {
        StringBuilder rotulo = new StringBuilder();
        int numero = indice + 1;
        while (numero > 0) {
            numero--;
            rotulo.insert(0, (char) ('A' + numero % 26));
            numero /= 26;
        }
        return rotulo.toString();
    }
}
