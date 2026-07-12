package br.uel.gacs.dao;

import br.uel.gacs.model.Coluna;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da entidade Coluna. */
public class ColunaDAO {
    public Long inserir(Coluna coluna) throws SQLException { try (Connection c=ConexaoBanco.obterConexaoBanco()){ return tx(c,()->inserir(c,coluna)); } }
    public Long inserir(Connection c, Coluna coluna) throws SQLException {
        String sql="INSERT INTO Coluna (idExperimento, rotulo, nomeColuna) VALUES (?, ?, ?)";
        try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            p.setLong(1,coluna.getIdExperimento()); if(coluna.getRotulo()==null)p.setNull(2,Types.SMALLINT);else p.setShort(2,coluna.getRotulo()); p.setString(3,coluna.getNomeColuna());
            if(p.executeUpdate()!=1)throw new SQLException("Não foi possível inserir a coluna.");
            try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("O banco não devolveu o ID da coluna inserida."); long id=r.getLong(1);coluna.setId(id);return id;}
        }
    }
    public void inserirTodos(Connection c,List<Coluna> colunas)throws SQLException{for(Coluna coluna:colunas)inserir(c,coluna);}
    public Optional<Coluna> buscarPorId(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return buscarPorId(c,id);}}
    public Optional<Coluna> buscarPorId(Connection c,Long id)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT id,idExperimento,rotulo,nomeColuna FROM Coluna WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(converter(r)):Optional.empty();}}
    }
    public List<Coluna> listarPorExperimento(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return listarPorExperimento(c,id);}}
    public List<Coluna> listarPorExperimento(Connection c,Long id)throws SQLException{
        List<Coluna> itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id,idExperimento,rotulo,nomeColuna FROM Coluna WHERE idExperimento=? ORDER BY rotulo,id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;
    }
    public boolean atualizar(Coluna coluna)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->atualizar(c,coluna));}}
    public boolean atualizar(Connection c,Coluna coluna)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE Coluna SET nomeColuna=? WHERE id=?")){p.setString(1,coluna.getNomeColuna());p.setLong(2,coluna.getId());return p.executeUpdate()==1;}}
    public boolean excluir(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->excluir(c,id));}}
    public boolean excluir(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM Coluna WHERE id=?")){p.setLong(1,id);return p.executeUpdate()==1;}}
    private Coluna converter(ResultSet r)throws SQLException{short rotulo=r.getShort("rotulo");return new Coluna(r.getLong("id"),r.getLong("idExperimento"),r.wasNull()?null:rotulo,r.getString("nomeColuna"));}
    private static <T>T tx(Connection c,Op<T>o)throws SQLException{boolean a=c.getAutoCommit();c.setAutoCommit(false);try{T v=o.run();c.commit();return v;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}throw e;}finally{try{c.setAutoCommit(a);}catch(SQLException ignored){}}}
    @FunctionalInterface private interface Op<T>{T run()throws SQLException;}
}
