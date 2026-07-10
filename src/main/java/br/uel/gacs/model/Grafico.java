package br.uel.gacs.model;

/** Representa uma composição gráfica salva. */
public class Grafico {
    private Long id;
    private String nome;
    public Grafico() { }
    public Grafico(Long id, String nome) { this.id = id; this.nome = nome; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
