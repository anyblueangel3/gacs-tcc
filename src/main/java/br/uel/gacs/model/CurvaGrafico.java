package br.uel.gacs.model;

/** Representa a associação ordenada entre uma curva e um gráfico. */
public class CurvaGrafico {
    private Long idGrafico;
    private Integer numeroCurva;
    private Long idCurva;

    public CurvaGrafico() { }
    public CurvaGrafico(Long idGrafico, Integer numeroCurva, Long idCurva) {
        validarNumeroCurva(numeroCurva);
        this.idGrafico = idGrafico; this.numeroCurva = numeroCurva; this.idCurva = idCurva;
    }
    public Long getIdGrafico() { return idGrafico; }
    public void setIdGrafico(Long idGrafico) { this.idGrafico = idGrafico; }
    public Integer getNumeroCurva() { return numeroCurva; }
    public void setNumeroCurva(Integer valor) { validarNumeroCurva(valor); this.numeroCurva = valor; }
    public Long getIdCurva() { return idCurva; }
    public void setIdCurva(Long idCurva) { this.idCurva = idCurva; }
    private static void validarNumeroCurva(Integer numero) {
        if (numero != null && numero < 1) {
            throw new IllegalArgumentException("O número da curva deve ser maior ou igual a 1.");
        }
    }
}
