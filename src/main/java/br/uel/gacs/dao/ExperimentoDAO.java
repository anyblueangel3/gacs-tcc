package br.uel.gacs.dao;

import br.uel.gacs.model.Experimento;
import java.sql.*;
import java.util.*;

/** Realiza as operações de persistência da entidade Experimento. */
public class ExperimentoDAO {
    public Long inserir(Experimento experimento) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            return executarTransacao(conexao, () -> inserir(conexao, experimento));
        }
    }

    public Long inserir(Connection conexao, Experimento experimento) throws SQLException {
        String sql = "INSERT INTO Experimento (nomeExperimento, dataExperimento, observacoes, idUsuario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, experimento.getNomeExperimento());
            comando.setTimestamp(2, Timestamp.valueOf(experimento.getDataExperimento()));
            comando.setString(3, experimento.getObservacoes());
            comando.setLong(4, experimento.getIdUsuario());
            exigirUmaLinha(comando.executeUpdate(), "inserir o experimento");
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) throw new SQLException("O banco não devolveu o ID do experimento inserido.");
                long id = chaves.getLong(1); experimento.setId(id); return id;
            }
        }
    }

    public Optional<Experimento> buscarPorId(Long id) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) { return buscarPorId(conexao, id); }
    }

    public Optional<Experimento> buscarPorId(Connection conexao, Long id) throws SQLException {
        String sql = "SELECT id, nomeExperimento, dataExperimento, observacoes, idUsuario FROM Experimento WHERE id = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            try (ResultSet r = comando.executeQuery()) { return r.next() ? Optional.of(converter(r)) : Optional.empty(); }
        }
    }

    public List<Experimento> listarPorUsuario(Long idUsuario) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) { return listarPorUsuario(conexao, idUsuario); }
    }

    public List<Experimento> listarPorUsuario(Connection conexao, Long idUsuario) throws SQLException {
        String sql = "SELECT id, nomeExperimento, dataExperimento, observacoes, idUsuario FROM Experimento WHERE idUsuario = ? ORDER BY dataExperimento DESC, id DESC";
        List<Experimento> itens = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idUsuario);
            try (ResultSet r = comando.executeQuery()) { while (r.next()) itens.add(converter(r)); }
        }
        return itens;
    }

    public List<Experimento> listarTodos() throws SQLException {
        String sql = "SELECT id, nomeExperimento, dataExperimento, observacoes, idUsuario "
                + "FROM Experimento ORDER BY dataExperimento DESC, id DESC";
        List<Experimento> itens = new ArrayList<>();
        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) itens.add(converter(resultado));
        }
        return itens;
    }

    public boolean atualizar(Experimento experimento) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            return executarTransacao(conexao, () -> atualizar(conexao, experimento));
        }
    }

    public boolean atualizar(Connection conexao, Experimento e) throws SQLException {
        String sql = "UPDATE Experimento SET nomeExperimento = ?, dataExperimento = ?, observacoes = ?, idUsuario = ? WHERE id = ?";
        try (PreparedStatement c = conexao.prepareStatement(sql)) {
            c.setString(1, e.getNomeExperimento()); c.setTimestamp(2, Timestamp.valueOf(e.getDataExperimento()));
            c.setString(3, e.getObservacoes()); c.setLong(4, e.getIdUsuario()); c.setLong(5, e.getId());
            return c.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long id) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) { return executarTransacao(conexao, () -> excluir(conexao, id)); }
    }

    public boolean excluir(Connection conexao, Long id) throws SQLException {
        try (PreparedStatement c = conexao.prepareStatement("DELETE FROM Experimento WHERE id = ?")) {
            c.setLong(1, id); return c.executeUpdate() == 1;
        }
    }

    private Experimento converter(ResultSet r) throws SQLException {
        return new Experimento(r.getLong("id"), r.getString("nomeExperimento"),
                r.getTimestamp("dataExperimento").toLocalDateTime(), r.getString("observacoes"), r.getLong("idUsuario"));
    }

    private static void exigirUmaLinha(int n, String operacao) throws SQLException { if (n != 1) throw new SQLException("Não foi possível " + operacao + "."); }
    private static <T> T executarTransacao(Connection c, Operacao<T> op) throws SQLException {
        boolean auto = c.getAutoCommit(); c.setAutoCommit(false);
        try { T resultado = op.executar(); c.commit(); return resultado; }
        catch (SQLException | RuntimeException e) { try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
        finally { try { c.setAutoCommit(auto); } catch (SQLException ignored) { } }
    }
    @FunctionalInterface private interface Operacao<T> { T executar() throws SQLException; }
}
