package br.uel.gacs.model;

/** Representa um valor medido em uma linha de uma coluna experimental. */
public class DadoColuna {
    private Long idColuna;
    private Integer numeroDaMedida;
    private Double valorMedida;

    public DadoColuna() { }
    public DadoColuna(Long idColuna, Integer numeroDaMedida, Double valorMedida) {
        validarNumeroDaMedida(numeroDaMedida);
        this.idColuna = idColuna; this.numeroDaMedida = numeroDaMedida; this.valorMedida = valorMedida;
    }
    public Long getIdColuna() { return idColuna; }
    public void setIdColuna(Long idColuna) { this.idColuna = idColuna; }
    public Integer getNumeroDaMedida() { return numeroDaMedida; }
    public void setNumeroDaMedida(Integer valor) { validarNumeroDaMedida(valor); this.numeroDaMedida = valor; }
    public Double getValorMedida() { return valorMedida; }
    public void setValorMedida(Double valorMedida) { this.valorMedida = valorMedida; }
    private static void validarNumeroDaMedida(Integer numero) {
        if (numero != null && (numero < 1 || numero > 10_000)) {
            throw new IllegalArgumentException("O número da medida deve estar entre 1 e 10.000.");
        }
    }
}
