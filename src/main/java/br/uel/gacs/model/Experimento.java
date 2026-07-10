package br.uel.gacs.model;

import java.time.LocalDateTime;

/**
 * Representa a unidade central de um trabalho experimental no GACS.
 */
public class Experimento {

    private Long idExperimento;
    private String nomeExperimento;
    private LocalDateTime dataExperimento;
    private String observacoes;

    public Experimento() {
    }

    public Experimento(Long idExperimento, String nomeExperimento,
                       LocalDateTime dataExperimento, String observacoes) {
        this.idExperimento = idExperimento;
        this.nomeExperimento = nomeExperimento;
        this.dataExperimento = dataExperimento;
        this.observacoes = observacoes;
    }

    public Long getIdExperimento() {
        return idExperimento;
    }

    public void setIdExperimento(Long idExperimento) {
        this.idExperimento = idExperimento;
    }

    public String getNomeExperimento() {
        return nomeExperimento;
    }

    public void setNomeExperimento(String nomeExperimento) {
        this.nomeExperimento = nomeExperimento;
    }

    public LocalDateTime getDataExperimento() {
        return dataExperimento;
    }

    public void setDataExperimento(LocalDateTime dataExperimento) {
        this.dataExperimento = dataExperimento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
