package br.uel.gacs.dao;

import br.uel.gacs.model.CurvaGrafico;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da associação CurvaGrafico. */
public class CurvaGraficoDAO {
    public void inserir(CurvaGrafico item)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){tx(c,()->{inserir(c,item);return null;});}}
    public void inserir(Connection c,CurvaGrafico item)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO CurvaGrafico (idGrafico,numeroCurva,idCurva) VALUES (?,?,?)")){p.setLong(1,item.getIdGrafico());p.setInt(2,item.getNumeroCurva());p.setLong(3,item.getIdCurva());if(p.executeUpdate()!=1)throw new SQLException("Não foi possível associar a curva ao gráfico.");}}
    public void inserirTodos(Connection c,List<CurvaGrafico> itens)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO CurvaGrafico (idGrafico,numeroCurva,idCurva) VALUES (?,?,?)")){for(CurvaGrafico i:itens){p.setLong(1,i.getIdGrafico());p.setInt(2,i.getNumeroCurva());p.setLong(3,i.getIdCurva());p.addBatch();}int[] n=p.executeBatch();for(int v:n)if(v==Statement.EXECUTE_FAILED)throw new SQLException("Falha ao associar as curvas ao gráfico.");}}
    public void inserirTodos(List<CurvaGrafico> itens)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){tx(c,()->{inserirTodos(c,itens);return null;});}}
    public Optional<CurvaGrafico> buscar(Long idGrafico,Integer numero)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco();PreparedStatement p=c.prepareStatement("SELECT idGrafico,numeroCurva,idCurva FROM CurvaGrafico WHERE idGrafico=? AND numeroCurva=?")){p.setLong(1,idGrafico);p.setInt(2,numero);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(converter(r)):Optional.empty();}}}
    public List<CurvaGrafico> listarPorGrafico(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){List<CurvaGrafico>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT idGrafico,numeroCurva,idCurva FROM CurvaGrafico WHERE idGrafico=? ORDER BY numeroCurva")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}}
    public List<CurvaGrafico> listarPorGrafico(Connection c,Long id)throws SQLException{List<CurvaGrafico>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT idGrafico,numeroCurva,idCurva FROM CurvaGrafico WHERE idGrafico=? ORDER BY numeroCurva")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}
    public boolean atualizar(CurvaGrafico item)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->atualizar(c,item));}}
    public boolean atualizar(Connection c,CurvaGrafico item)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE CurvaGrafico SET idCurva=? WHERE idGrafico=? AND numeroCurva=?")){p.setLong(1,item.getIdCurva());p.setLong(2,item.getIdGrafico());p.setInt(3,item.getNumeroCurva());return p.executeUpdate()==1;}}
    public boolean excluir(Long idGrafico,Integer numero)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->excluir(c,idGrafico,numero));}}
    public boolean excluir(Connection c,Long idGrafico,Integer numero)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM CurvaGrafico WHERE idGrafico=? AND numeroCurva=?")){p.setLong(1,idGrafico);p.setInt(2,numero);return p.executeUpdate()==1;}}
    public int excluirPorGrafico(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM CurvaGrafico WHERE idGrafico=?")){p.setLong(1,id);return p.executeUpdate();}}
    public int contarReferenciasDaCurva(Connection c,Long idCurva)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM CurvaGrafico WHERE idCurva=?")){p.setLong(1,idCurva);try(ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}}
    private CurvaGrafico converter(ResultSet r)throws SQLException{return new CurvaGrafico(r.getLong("idGrafico"),r.getInt("numeroCurva"),r.getLong("idCurva"));}
    private static <T>T tx(Connection c,Op<T>o)throws SQLException{boolean a=c.getAutoCommit();c.setAutoCommit(false);try{T v=o.run();c.commit();return v;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}throw e;}finally{try{c.setAutoCommit(a);}catch(SQLException ignored){}}}
    @FunctionalInterface private interface Op<T>{T run()throws SQLException;}
}
