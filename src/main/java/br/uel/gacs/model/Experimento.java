package br.uel.gacs.model;

import java.time.LocalDateTime;

/** Representa a unidade central de um trabalho experimental. */
public class Experimento {
    private Long id;
    private String nomeExperimento;
    private LocalDateTime dataExperimento;
    private String observacoes;
    private Long idUsuario;

    public Experimento() { }
    public Experimento(Long id, String nomeExperimento, LocalDateTime dataExperimento,
                       String observacoes, Long idUsuario) {
        this.id = id; this.nomeExperimento = nomeExperimento; this.dataExperimento = dataExperimento;
        this.observacoes = observacoes; this.idUsuario = idUsuario;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeExperimento() { return nomeExperimento; }
    public void setNomeExperimento(String valor) { this.nomeExperimento = valor; }
    public LocalDateTime getDataExperimento() { return dataExperimento; }
    public void setDataExperimento(LocalDateTime valor) { this.dataExperimento = valor; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
