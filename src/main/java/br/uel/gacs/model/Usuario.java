package br.uel.gacs.model;

import java.time.LocalDateTime;

/** Representa um usuário autorizado a utilizar o GACS. */
public class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String senhaHash;
    private PerfilUsuario perfil;
    private Boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAlteracao;

    public Usuario() { }

    public Usuario(Long id, String nome, String email, String senhaHash, PerfilUsuario perfil,
                   Boolean ativo, LocalDateTime dataCriacao, LocalDateTime dataUltimaAlteracao) {
        this.id = id; this.nome = nome; this.email = email; this.senhaHash = senhaHash;
        this.perfil = perfil; this.ativo = ativo; this.dataCriacao = dataCriacao;
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime valor) { this.dataCriacao = valor; }
    public LocalDateTime getDataUltimaAlteracao() { return dataUltimaAlteracao; }
    public void setDataUltimaAlteracao(LocalDateTime valor) { this.dataUltimaAlteracao = valor; }
}
