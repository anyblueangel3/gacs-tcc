package br.uel.gacs.controller;

import br.uel.gacs.dao.UsuarioDAO;
import br.uel.gacs.model.PerfilUsuario;
import br.uel.gacs.model.Usuario;
import br.uel.gacs.util.SenhaUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Coordena as operações e as regras relacionadas aos usuários do GACS. */
public class UsuarioCtlr {
    private final UsuarioDAO usuarioDAO;

    public UsuarioCtlr() {
        this(new UsuarioDAO());
    }

    /** Permite fornecer outro DAO, facilitando testes do controller. */
    public UsuarioCtlr(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("O UsuarioDAO deve ser informado.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Cadastra um usuário. O primeiro usuário do sistema recebe obrigatoriamente
     * o perfil ADMINISTRADOR.
     */
    public Usuario cadastrar(String nome, String email, String senha,
                             PerfilUsuario perfil) throws SQLException {
        String nomeNormalizado = normalizarNome(nome);
        String emailNormalizado = normalizarEmail(email);
        validarSenha(senha);

        if (usuarioDAO.existeEmail(emailNormalizado)) {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }

        boolean primeiroUsuario = !usuarioDAO.possuiUsuarios();
        PerfilUsuario perfilDefinido = primeiroUsuario
                ? PerfilUsuario.ADMINISTRADOR
                : exigirPerfil(perfil);
        LocalDateTime agora = LocalDateTime.now();

        Usuario usuario = new Usuario(
                null,
                nomeNormalizado,
                emailNormalizado,
                SenhaUtil.gerarHash(senha),
                perfilDefinido,
                true,
                agora,
                agora);

        usuarioDAO.inserir(usuario);
        return usuario;
    }

    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        validarId(id);
        return usuarioDAO.buscarPorId(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        return usuarioDAO.buscarPorEmail(normalizarEmail(email));
    }

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    /** Atualiza os dados do usuário e conserva a senha atual. */
    public boolean atualizar(Usuario usuario) throws SQLException {
        validarUsuarioParaAtualizacao(usuario);
        normalizarUsuario(usuario);

        Optional<Usuario> usuarioComMesmoEmail = usuarioDAO.buscarPorEmail(usuario.getEmail());
        if (usuarioComMesmoEmail.isPresent()
                && !usuarioComMesmoEmail.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Já existe outro usuário com este e-mail.");
        }

        usuario.setDataUltimaAlteracao(LocalDateTime.now());
        return usuarioDAO.atualizar(usuario);
    }

    /** Substitui a senha pelo hash da nova senha informada. */
    public boolean alterarSenha(Long id, String novaSenha) throws SQLException {
        validarId(id);
        validarSenha(novaSenha);

        Optional<Usuario> usuarioEncontrado = usuarioDAO.buscarPorId(id);
        if (usuarioEncontrado.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioEncontrado.get();
        usuario.setSenhaHash(SenhaUtil.gerarHash(novaSenha));
        usuario.setDataUltimaAlteracao(LocalDateTime.now());
        return usuarioDAO.atualizar(usuario);
    }

    public boolean alterarStatus(Long id, boolean ativo) throws SQLException {
        validarId(id);
        return usuarioDAO.alterarStatus(id, ativo);
    }

    public boolean existeEmail(String email) throws SQLException {
        return usuarioDAO.existeEmail(normalizarEmail(email));
    }

    public boolean possuiUsuarios() throws SQLException {
        return usuarioDAO.possuiUsuarios();
    }

    private void validarUsuarioParaAtualizacao(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário deve ser informado.");
        }

        validarId(usuario.getId());
        normalizarNome(usuario.getNome());
        normalizarEmail(usuario.getEmail());
        exigirPerfil(usuario.getPerfil());

        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isBlank()) {
            throw new IllegalArgumentException("O hash da senha deve estar preenchido.");
        }

        if (usuario.getAtivo() == null) {
            throw new IllegalArgumentException("A situação do usuário deve ser informada.");
        }
    }

    private void normalizarUsuario(Usuario usuario) {
        usuario.setNome(normalizarNome(usuario.getNome()));
        usuario.setEmail(normalizarEmail(usuario.getEmail()));
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome deve ser preenchido.");
        }
        return nome.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail deve ser preenchido.");
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        int arroba = emailNormalizado.indexOf('@');
        if (arroba <= 0
                || arroba != emailNormalizado.lastIndexOf('@')
                || arroba == emailNormalizado.length() - 1) {
            throw new IllegalArgumentException("O e-mail informado é inválido.");
        }
        return emailNormalizado;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha deve ser preenchida.");
        }
    }

    private PerfilUsuario exigirPerfil(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil deve ser informado.");
        }
        return perfil;
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID do usuário deve ser válido.");
        }
    }
}
