package br.uel.gacs.model;

/** Registra a especialização de uma curva usada na caracterização de FET. */
public class CurvaFet {
    private Long idCurva;
    private TipoCurvaFet tipoCurvaFet;
    private Double valorTensaoConstante;

    public CurvaFet() { }

    public CurvaFet(Long idCurva, TipoCurvaFet tipoCurvaFet,
                    Double valorTensaoConstante) {
        validarTensao(valorTensaoConstante);
        this.idCurva = idCurva;
        this.tipoCurvaFet = tipoCurvaFet;
        this.valorTensaoConstante = valorTensaoConstante;
    }

    public Long getIdCurva() {
        return idCurva;
    }

    public void setIdCurva(Long idCurva) {
        this.idCurva = idCurva;
    }

    public TipoCurvaFet getTipoCurvaFet() {
        return tipoCurvaFet;
    }

    public void setTipoCurvaFet(TipoCurvaFet tipoCurvaFet) {
        this.tipoCurvaFet = tipoCurvaFet;
    }

    public Double getValorTensaoConstante() {
        return valorTensaoConstante;
    }

    public void setValorTensaoConstante(Double valorTensaoConstante) {
        validarTensao(valorTensaoConstante);
        this.valorTensaoConstante = valorTensaoConstante;
    }

    private static void validarTensao(Double valor) {
        if (valor != null && !Double.isFinite(valor)) {
            throw new IllegalArgumentException("A tensão constante deve ser um número finito.");
        }
    }
}
