package br.uel.gacs.dao;

import br.uel.gacs.model.DadoColuna;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da entidade DadoColuna. */
public class DadoColunaDAO {
    public void inserir(DadoColuna dado)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){tx(c,()->{inserir(c,dado);return null;});}}
    public void inserir(Connection c,DadoColuna d)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO DadoColuna (idColuna,numeroDaMedida,valorMedida) VALUES (?,?,?)")){parametros(p,d);if(p.executeUpdate()!=1)throw new SQLException("Não foi possível inserir o dado da coluna.");}}
    public void inserirTodos(List<DadoColuna> dados)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){tx(c,()->{inserirTodos(c,dados);return null;});}}
    public void inserirTodos(Connection c,List<DadoColuna> dados)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO DadoColuna (idColuna,numeroDaMedida,valorMedida) VALUES (?,?,?)")){for(DadoColuna d:dados){parametros(p,d);p.addBatch();}int[] n=p.executeBatch();for(int v:n)if(v==Statement.EXECUTE_FAILED)throw new SQLException("Falha ao inserir os dados da coluna.");}}
    public Optional<DadoColuna> buscar(Long idColuna,Integer numero)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco();PreparedStatement p=c.prepareStatement("SELECT idColuna,numeroDaMedida,valorMedida FROM DadoColuna WHERE idColuna=? AND numeroDaMedida=?")){p.setLong(1,idColuna);p.setInt(2,numero);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(converter(r)):Optional.empty();}}}
    public List<DadoColuna> listarPorColuna(Long id)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){List<DadoColuna>itens=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT idColuna,numeroDaMedida,valorMedida FROM DadoColuna WHERE idColuna=? ORDER BY numeroDaMedida")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())itens.add(converter(r));}}return itens;}}
    public boolean atualizar(DadoColuna d)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->atualizar(c,d));}}
    public boolean atualizar(Connection c,DadoColuna d)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE DadoColuna SET valorMedida=? WHERE idColuna=? AND numeroDaMedida=?")){p.setDouble(1,d.getValorMedida());p.setLong(2,d.getIdColuna());p.setInt(3,d.getNumeroDaMedida());return p.executeUpdate()==1;}}
    public boolean excluir(Long idColuna,Integer numero)throws SQLException{try(Connection c=ConexaoBanco.obterConexaoBanco()){return tx(c,()->excluir(c,idColuna,numero));}}
    public boolean excluir(Connection c,Long idColuna,Integer numero)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM DadoColuna WHERE idColuna=? AND numeroDaMedida=?")){p.setLong(1,idColuna);p.setInt(2,numero);return p.executeUpdate()==1;}}
    public int excluirPorColuna(Connection c,Long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM DadoColuna WHERE idColuna=?")){p.setLong(1,id);return p.executeUpdate();}}
    private void parametros(PreparedStatement p,DadoColuna d)throws SQLException{p.setLong(1,d.getIdColuna());p.setInt(2,d.getNumeroDaMedida());p.setDouble(3,d.getValorMedida());}
    private DadoColuna converter(ResultSet r)throws SQLException{return new DadoColuna(r.getLong("idColuna"),r.getInt("numeroDaMedida"),r.getDouble("valorMedida"));}
    private static <T>T tx(Connection c,Op<T>o)throws SQLException{boolean a=c.getAutoCommit();c.setAutoCommit(false);try{T v=o.run();c.commit();return v;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}throw e;}finally{try{c.setAutoCommit(a);}catch(SQLException ignored){}}}
    @FunctionalInterface private interface Op<T>{T run()throws SQLException;}
}
