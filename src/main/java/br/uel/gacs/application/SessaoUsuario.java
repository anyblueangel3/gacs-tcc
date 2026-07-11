package br.uel.gacs.application;

import br.uel.gacs.model.Usuario;

import java.util.Optional;

/** Mantém em memória o usuário autenticado durante a execução do GACS. */
public final class SessaoUsuario {
    private static Usuario usuarioLogado;

    private SessaoUsuario() { }

    /** Inicia a sessão com um usuário ativo. */
    public static void iniciar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário deve ser informado.");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException("Não é possível iniciar sessão com usuário inativo.");
        }

        usuarioLogado = usuario;
    }

    /** Encerra a sessão atual. */
    public static void encerrar() {
        usuarioLogado = null;
    }

    /** Informa se existe um usuário autenticado. */
    public static boolean possuiUsuarioLogado() {
        return usuarioLogado != null;
    }

    /** Devolve o usuário autenticado, quando houver. */
    public static Optional<Usuario> getUsuarioLogado() {
        return Optional.ofNullable(usuarioLogado);
    }

    /**
     * Devolve obrigatoriamente o usuário autenticado.
     *
     * @throws IllegalStateException quando não existe sessão iniciada
     */
    public static Usuario exigirUsuarioLogado() {
        if (usuarioLogado == null) {
            throw new IllegalStateException("Não há usuário autenticado.");
        }
        return usuarioLogado;
    }
}
