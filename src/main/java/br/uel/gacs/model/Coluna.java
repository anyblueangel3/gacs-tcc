package br.uel.gacs.model;

/**
 * Representa uma coluna de grandeza ou de dados experimentais.
 */
public class Coluna {

    private Long idColuna;
    private Long idExperimento;
    private String nomeColuna;
    private TipoEixo tipoEixo;
    private Long idColunaX;

    public Coluna() {
    }

    public Coluna(Long idColuna, Long idExperimento, String nomeColuna,
                  TipoEixo tipoEixo, Long idColunaX) {
        validarAssociacaoEixo(tipoEixo, idColunaX);
        this.idColuna = idColuna;
        this.idExperimento = idExperimento;
        this.nomeColuna = nomeColuna;
        this.tipoEixo = tipoEixo;
        this.idColunaX = idColunaX;
    }

    public Long getIdColuna() {
        return idColuna;
    }

    public void setIdColuna(Long idColuna) {
        this.idColuna = idColuna;
    }

    public Long getIdExperimento() {
        return idExperimento;
    }

    public void setIdExperimento(Long idExperimento) {
        this.idExperimento = idExperimento;
    }

    public String getNomeColuna() {
        return nomeColuna;
    }

    public void setNomeColuna(String nomeColuna) {
        this.nomeColuna = nomeColuna;
    }

    public TipoEixo getTipoEixo() {
        return tipoEixo;
    }

    public void setTipoEixo(TipoEixo tipoEixo) {
        validarAssociacaoEixo(tipoEixo, idColunaX);
        this.tipoEixo = tipoEixo;
    }

    public Long getIdColunaX() {
        return idColunaX;
    }

    public void setIdColunaX(Long idColunaX) {
        validarAssociacaoEixo(tipoEixo, idColunaX);
        this.idColunaX = idColunaX;
    }

    /**
     * Verifica se o papel da coluna é compatível com sua referência ao eixo X.
     * Estados sem tipo definido são permitidos durante o preenchimento por JDBC
     * ou formulários.
     *
     * @param tipoEixo papel da coluna
     * @param idColunaX identificador da coluna X associada
     * @throws IllegalArgumentException se uma coluna X possuir referência a X
     *                                  ou uma coluna Y não possuir essa referência
     */
    private static void validarAssociacaoEixo(TipoEixo tipoEixo, Long idColunaX) {
        if (tipoEixo == TipoEixo.X && idColunaX != null) {
            throw new IllegalArgumentException("Uma coluna do eixo X não pode referenciar outra coluna X.");
        }
        if (tipoEixo == TipoEixo.Y && idColunaX == null) {
            throw new IllegalArgumentException("Uma coluna do eixo Y deve referenciar uma coluna X.");
        }
    }
}
