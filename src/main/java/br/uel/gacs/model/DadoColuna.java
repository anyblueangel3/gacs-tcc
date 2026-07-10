package br.uel.gacs.model;

/**
 * Representa um valor medido pertencente a uma coluna experimental.
 */
public class DadoColuna {

    private Long idDado;
    private Long idColuna;
    private Integer numeroDaMedida;
    private Double valorMedida;

    public DadoColuna() {
    }

    public DadoColuna(Long idDado, Long idColuna, Integer numeroDaMedida,
                      Double valorMedida) {
        this.idDado = idDado;
        this.idColuna = idColuna;
        this.numeroDaMedida = numeroDaMedida;
        this.valorMedida = valorMedida;
    }

    public Long getIdDado() {
        return idDado;
    }

    public void setIdDado(Long idDado) {
        this.idDado = idDado;
    }

    public Long getIdColuna() {
        return idColuna;
    }

    public void setIdColuna(Long idColuna) {
        this.idColuna = idColuna;
    }

    public Integer getNumeroDaMedida() {
        return numeroDaMedida;
    }

    public void setNumeroDaMedida(Integer numeroDaMedida) {
        this.numeroDaMedida = numeroDaMedida;
    }

    public Double getValorMedida() {
        return valorMedida;
    }

    public void setValorMedida(Double valorMedida) {
        this.valorMedida = valorMedida;
    }
}
