package br.uel.gacs.model;

import java.util.Objects;

/** Representa a associação entre uma coluna X e uma coluna Y. */
public class Curva {
    private Long id;
    private String nome;
    private Long idColunaX;
    private Long idColunaY;

    public Curva() { }
    public Curva(Long id, String nome, Long idColunaX, Long idColunaY) {
        validarColunas(idColunaX, idColunaY);
        this.id = id; this.nome = nome; this.idColunaX = idColunaX; this.idColunaY = idColunaY;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Long getIdColunaX() { return idColunaX; }
    public void setIdColunaX(Long valor) { validarColunas(valor, idColunaY); this.idColunaX = valor; }
    public Long getIdColunaY() { return idColunaY; }
    public void setIdColunaY(Long valor) { validarColunas(idColunaX, valor); this.idColunaY = valor; }
    private static void validarColunas(Long idColunaX, Long idColunaY) {
        if (idColunaX != null && Objects.equals(idColunaX, idColunaY)) {
            throw new IllegalArgumentException("As colunas X e Y devem ser diferentes.");
        }
    }
}
