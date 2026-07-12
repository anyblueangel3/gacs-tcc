package br.uel.gacs.model;

/** Representa uma composição gráfica salva. */
public class Grafico {
    private Long id;
    private Long idExperimento;
    private String nome;
    public Grafico() { }
    public Grafico(Long id, Long idExperimento, String nome) {
        this.id = id; this.idExperimento = idExperimento; this.nome = nome;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdExperimento() { return idExperimento; }
    public void setIdExperimento(Long idExperimento) { this.idExperimento = idExperimento; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
