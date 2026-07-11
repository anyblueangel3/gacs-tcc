package br.uel.gacs.dao;

import br.uel.gacs.model.PerfilUsuario;
import br.uel.gacs.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Realiza as operações de persistência da entidade Usuario. */
public class UsuarioDAO {

    /** Insere um usuário e devolve o identificador gerado pelo MySQL. */
    public Long inserir(Usuario usuario) throws SQLException {
        String sql = """
                INSERT INTO Usuario
                    (nome, email, senhaHash, perfil, ativo, dataCriacao, dataUltimaAlteracao)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenhaHash());
            comando.setString(4, usuario.getPerfil().name());
            comando.setBoolean(5, usuario.getAtivo());
            comando.setTimestamp(6, Timestamp.valueOf(usuario.getDataCriacao()));
            comando.setTimestamp(7, Timestamp.valueOf(usuario.getDataUltimaAlteracao()));

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas != 1) {
                throw new SQLException("Não foi possível inserir o usuário.");
            }

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco não devolveu o ID do usuário inserido.");
                }

                Long idGerado = chaves.getLong(1);
                usuario.setId(idGerado);
                return idGerado;
            }
        }
    }

    /** Busca um usuário pelo identificador. */
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        String sql = """
                SELECT id, nome, email, senhaHash, perfil, ativo,
                       dataCriacao, dataUltimaAlteracao
                FROM Usuario
                WHERE id = ?
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next()
                        ? Optional.of(converterUsuario(resultado))
                        : Optional.empty();
            }
        }
    }

    /** Busca um usuário pelo e-mail. */
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = """
                SELECT id, nome, email, senhaHash, perfil, ativo,
                       dataCriacao, dataUltimaAlteracao
                FROM Usuario
                WHERE email = ?
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next()
                        ? Optional.of(converterUsuario(resultado))
                        : Optional.empty();
            }
        }
    }

    /** Lista todos os usuários em ordem alfabética de nome. */
    public List<Usuario> listarTodos() throws SQLException {
        String sql = """
                SELECT id, nome, email, senhaHash, perfil, ativo,
                       dataCriacao, dataUltimaAlteracao
                FROM Usuario
                ORDER BY nome, id
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(converterUsuario(resultado));
            }
        }

        return usuarios;
    }

    /** Atualiza os dados de um usuário existente. */
    public boolean atualizar(Usuario usuario) throws SQLException {
        String sql = """
                UPDATE Usuario
                SET nome = ?,
                    email = ?,
                    senhaHash = ?,
                    perfil = ?,
                    ativo = ?,
                    dataUltimaAlteracao = ?
                WHERE id = ?
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenhaHash());
            comando.setString(4, usuario.getPerfil().name());
            comando.setBoolean(5, usuario.getAtivo());
            comando.setTimestamp(6, Timestamp.valueOf(usuario.getDataUltimaAlteracao()));
            comando.setLong(7, usuario.getId());

            return comando.executeUpdate() == 1;
        }
    }

    /** Ativa ou desativa um usuário. */
    public boolean alterarStatus(Long id, boolean ativo) throws SQLException {
        String sql = """
                UPDATE Usuario
                SET ativo = ?, dataUltimaAlteracao = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setBoolean(1, ativo);
            comando.setLong(2, id);
            return comando.executeUpdate() == 1;
        }
    }

    /** Informa se já existe um usuário com o e-mail indicado. */
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM Usuario WHERE email = ? LIMIT 1";

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }

    /** Informa se há pelo menos um usuário cadastrado. */
    public boolean possuiUsuarios() throws SQLException {
        String sql = "SELECT 1 FROM Usuario LIMIT 1";

        try (Connection conexao = ConexaoBanco.obterConexaoBanco();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            return resultado.next();
        }
    }

    private Usuario converterUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getLong("id"),
                resultado.getString("nome"),
                resultado.getString("email"),
                resultado.getString("senhaHash"),
                PerfilUsuario.valueOf(resultado.getString("perfil")),
                resultado.getBoolean("ativo"),
                resultado.getTimestamp("dataCriacao").toLocalDateTime(),
                resultado.getTimestamp("dataUltimaAlteracao").toLocalDateTime());
    }
}
