package br.uel.gacs.dao;

import br.uel.gacs.model.Curva;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da entidade Curva. */
public class CurvaDAO {
    public Long inserir(Curva curva)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->inserir(c,curva));}}
    public Long inserir(Connection c,Curva curva)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO Curva (nome,idColunaX,idColunaY) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,curva.getNome());p.setLong(2,curva.getIdColunaX());p.setLong(3,curva.getIdColunaY());if(p.executeUpdate()!=1)throw new SQLException("Não foi possível inserir a curva.");try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("O banco não devolveu o ID da curva inserida.");long id=r.getLong(1);curva.setId(id);return id;}}}
    public Optional<Curva> buscarPorId(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return buscarPorId(c,id);}}
    public Optional<Curva> buscarPorId(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT id,nome,idColunaX,idColunaY FROM Curva WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(converter(r)):Optional.empty();}}}
    public List<Curva> listarPorExperimento(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){List<Curva>itens=new ArrayList<>();String sql="SELECT c.id,c.nome,c.idColunaX,c.idColunaY FROM Curva c JOIN Coluna x ON x.id=c.idColunaX JOIN Coluna y ON y.id=c.idColunaY WHERE x.idExperimento=? AND y.idExperimento=? ORDER BY c.nome,c.id";try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);p.setLong(2,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}}
    public List<Curva> listarPorExperimento(Connection c,Long id)throws SQLException{List<Curva>itens=new ArrayList<>();String sql="SELECT cv.id,cv.nome,cv.idColunaX,cv.idColunaY FROM Curva cv JOIN Coluna x ON x.id=cv.idColunaX JOIN Coluna y ON y.id=cv.idColunaY WHERE x.idExperimento=? AND y.idExperimento=? ORDER BY cv.nome,cv.id";try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);p.setLong(2,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}
    public List<Curva> listarPorColuna(Connection c,Long idColuna)throws SQLException{List<Curva>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id,nome,idColunaX,idColunaY FROM Curva WHERE idColunaX=? OR idColunaY=?")){p.setLong(1,idColuna);p.setLong(2,idColuna);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}
    public boolean atualizar(Curva curva)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->atualizar(c,curva));}}
    public boolean atualizar(Connection c,Curva curva)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE Curva SET nome=?,idColunaX=?,idColunaY=? WHERE id=?")){p.setString(1,curva.getNome());p.setLong(2,curva.getIdColunaX());p.setLong(3,curva.getIdColunaY());p.setLong(4,curva.getId());return p.executeUpdate()==1;}}
    public boolean excluir(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->excluir(c,id));}}
    public boolean excluir(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM Curva WHERE id=?")){p.setLong(1,id);return p.executeUpdate()==1;}}
    private Curva converter(ResultSet r)throws SQLException{return new Curva(r.getLong("id"),r.getString("nome"),r.getLong("idColunaX"),r.getLong("idColunaY"));}
    private static <T>T tx(Connection c,Op<T>o)throws SQLException{boolean a=c.getAutoCommit();c.setAutoCommit(false);try{T v=o.run();c.commit();return v;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}throw e;}finally{try{c.setAutoCommit(a);}catch(SQLException ignored){}}}
    @FunctionalInterface private interface Op<T>{T run()throws SQLException;}
}
