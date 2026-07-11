package br.uel.gacs.controller;

import br.uel.gacs.dao.UsuarioDAO;
import br.uel.gacs.model.Usuario;
import br.uel.gacs.util.SenhaUtil;
import br.uel.gacs.application.SessaoUsuario;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

/** Coordena a autenticação dos usuários do GACS. */
public class LoginCtlr {
    private final UsuarioDAO usuarioDAO;

    public LoginCtlr() {
        this(new UsuarioDAO());
    }

    /** Permite fornecer outro DAO, facilitando testes do controller. */
    public LoginCtlr(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("O UsuarioDAO deve ser informado.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Autentica um usuário ativo e inicia sua sessão.
     *
     * @return o usuário autenticado ou Optional.empty() quando as credenciais
     *         forem inválidas ou o usuário estiver inativo
     */
    public Optional<Usuario> autenticar(String email, String senha) throws SQLException {
        String emailNormalizado = normalizarEmail(email);

        if (senha == null || senha.isEmpty()) {
            return Optional.empty();
        }

        Optional<Usuario> usuarioEncontrado = usuarioDAO.buscarPorEmail(emailNormalizado);

        if (usuarioEncontrado.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!Boolean.TRUE.equals(usuario.getAtivo())
                || !SenhaUtil.verificarSenha(senha, usuario.getSenhaHash())) {
            return Optional.empty();
        }

        SessaoUsuario.iniciar(usuario);
        return Optional.of(usuario);
    }

    /** Encerra a sessão do usuário atual. */
    public void sair() {
        SessaoUsuario.encerrar();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
