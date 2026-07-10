package br.uel.gacs.model;

/** Representa uma coluna de dados pertencente a um experimento. */
public class Coluna {
    private Long id;
    private Long idExperimento;
    private Short rotulo;
    private String nomeColuna;

    public Coluna() { }
    public Coluna(Long id, Long idExperimento, Short rotulo, String nomeColuna) {
        validarRotulo(rotulo);
        this.id = id; this.idExperimento = idExperimento; this.rotulo = rotulo;
        this.nomeColuna = nomeColuna;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdExperimento() { return idExperimento; }
    public void setIdExperimento(Long valor) { this.idExperimento = valor; }
    public Short getRotulo() { return rotulo; }
    public void setRotulo(Short rotulo) { validarRotulo(rotulo); this.rotulo = rotulo; }
    public String getNomeColuna() { return nomeColuna; }
    public void setNomeColuna(String valor) { this.nomeColuna = valor; }
    private static void validarRotulo(Short rotulo) {
        if (rotulo != null && (rotulo < 1 || rotulo > 50)) {
            throw new IllegalArgumentException("O rótulo deve estar entre 1 e 50.");
        }
    }
}
