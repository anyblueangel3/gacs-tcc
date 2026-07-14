package br.uel.gacs.dao;

import br.uel.gacs.model.Grafico;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da entidade Grafico. */
public class GraficoDAO {
    public Long inserir(Grafico grafico)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->inserir(c,grafico));}}
    public Long inserir(Connection c,Grafico g)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO Grafico (idExperimento,nome) VALUES (?,?)",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,g.getIdExperimento());p.setString(2,g.getNome());if(p.executeUpdate()!=1)throw new SQLException("Não foi possível inserir o gráfico.");try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("O banco não devolveu o ID do gráfico inserido.");long id=r.getLong(1);g.setId(id);return id;}}}
    public Optional<Grafico> buscarPorId(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return buscarPorId(c,id);}}
    public Optional<Grafico> buscarPorId(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id,idExperimento,nome FROM Grafico WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(converter(r)):Optional.empty();}}}
    public List<Grafico> listarPorExperimento(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){List<Grafico>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id,idExperimento,nome FROM Grafico WHERE idExperimento=? ORDER BY nome,id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}}
    public List<Grafico> listarPorExperimento(Connection c,Long id)throws SQLException{List<Grafico>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id,idExperimento,nome FROM Grafico WHERE idExperimento=? ORDER BY nome,id")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}
    public boolean atualizar(Grafico g)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->atualizar(c,g));}}
    public boolean atualizar(Connection c,Grafico g)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE Grafico SET idExperimento=?,nome=? WHERE id=?")){p.setLong(1,g.getIdExperimento());p.setString(2,g.getNome());p.setLong(3,g.getId());return p.executeUpdate()==1;}}
    public boolean excluir(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->excluir(c,id));}}
    public boolean excluir(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM Grafico WHERE id=?")){p.setLong(1,id);return p.executeUpdate()==1;}}
    private Grafico converter(ResultSet r)throws SQLException{return new Grafico(r.getLong("id"),r.getLong("idExperimento"),r.getString("nome"));}
    private static <T>T tx(Connection c,Op<T>o)throws SQLException{boolean a=c.getAutoCommit();c.setAutoCommit(false);try{T v=o.run();c.commit();return v;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}throw e;}finally{try{c.setAutoCommit(a);}catch(SQLException ignored){}}}
    @FunctionalInterface private interface Op<T>{T run()throws SQLException;}
}
