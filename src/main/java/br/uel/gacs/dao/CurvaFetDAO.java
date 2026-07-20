package br.uel.gacs.dao;

import br.uel.gacs.model.CurvaFet;
import br.uel.gacs.model.TipoCurvaFet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Realiza as operações de persistência da especialização CurvaFet. */
public class CurvaFetDAO {

    public void inserir(CurvaFet curvaFet) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            executarTransacao(conexao, () -> {
                inserir(conexao, curvaFet);
                return null;
            });
        }
    }

    public void inserir(Connection conexao, CurvaFet curvaFet) throws SQLException {
        String sql = "INSERT INTO CurvaFet "
                + "(idCurva, tipoCurvaFet, valorTensaoConstante) VALUES (?, ?, ?)";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, curvaFet.getIdCurva());
            comando.setString(2, curvaFet.getTipoCurvaFet().name());
            comando.setDouble(3, curvaFet.getValorTensaoConstante());

            if (comando.executeUpdate() != 1) {
                throw new SQLException("Não foi possível registrar a curva de FET.");
            }
        }
    }

    public Optional<CurvaFet> buscarPorCurva(Long idCurva) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            return buscarPorCurva(conexao, idCurva);
        }
    }

    public Optional<CurvaFet> buscarPorCurva(Connection conexao, Long idCurva)
            throws SQLException {
        String sql = "SELECT idCurva, tipoCurvaFet, valorTensaoConstante "
                + "FROM CurvaFet WHERE idCurva = ?";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idCurva);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next()
                        ? Optional.of(converter(resultado))
                        : Optional.empty();
            }
        }
    }

    public List<CurvaFet> listarPorExperimento(Long idExperimento) throws SQLException {
        String sql = """
                SELECT cf.idCurva, cf.tipoCurvaFet, cf.valorTensaoConstante
                FROM CurvaFet cf
                JOIN Curva c ON c.id = cf.idCurva
                JOIN Coluna x ON x.id = c.idColunaX
                JOIN Coluna y ON y.id = c.idColunaY
                WHERE x.idExperimento = ? AND y.idExperimento = ?
                ORDER BY cf.tipoCurvaFet, cf.valorTensaoConstante, cf.idCurva
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idExperimento);
            comando.setLong(2, idExperimento);
            List<CurvaFet> curvas = new ArrayList<>();
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    curvas.add(converter(resultado));
                }
            }
            return curvas;
        }
    }

    public boolean atualizar(CurvaFet curvaFet) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            return executarTransacao(conexao, () -> atualizar(conexao, curvaFet));
        }
    }

    public boolean atualizar(Connection conexao, CurvaFet curvaFet) throws SQLException {
        String sql = "UPDATE CurvaFet SET tipoCurvaFet = ?, "
                + "valorTensaoConstante = ? WHERE idCurva = ?";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, curvaFet.getTipoCurvaFet().name());
            comando.setDouble(2, curvaFet.getValorTensaoConstante());
            comando.setLong(3, curvaFet.getIdCurva());
            return comando.executeUpdate() == 1;
        }
    }

    public boolean excluir(Long idCurva) throws SQLException {
        try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
            return executarTransacao(conexao, () -> excluir(conexao, idCurva));
        }
    }

    public boolean excluir(Connection conexao, Long idCurva) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "DELETE FROM CurvaFet WHERE idCurva = ?")) {
            comando.setLong(1, idCurva);
            return comando.executeUpdate() == 1;
        }
    }

    private CurvaFet converter(ResultSet resultado) throws SQLException {
        return new CurvaFet(
                resultado.getLong("idCurva"),
                TipoCurvaFet.valueOf(resultado.getString("tipoCurvaFet")),
                resultado.getDouble("valorTensaoConstante"));
    }

    private static <T> T executarTransacao(Connection conexao, Operacao<T> operacao)
            throws SQLException {
        boolean autoCommitOriginal = conexao.getAutoCommit();
        conexao.setAutoCommit(false);
        try {
            T resultado = operacao.executar();
            conexao.commit();
            return resultado;
        } catch (SQLException | RuntimeException erro) {
            try {
                conexao.rollback();
            } catch (SQLException erroRollback) {
                erro.addSuppressed(erroRollback);
            }
            throw erro;
        } finally {
            try {
                conexao.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
                // A conexão será fechada pelo chamador.
            }
        }
    }

    @FunctionalInterface
    private interface Operacao<T> {
        T executar() throws SQLException;
    }
}
