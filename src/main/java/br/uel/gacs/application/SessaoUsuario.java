package br.uel.gacs.application;

import br.uel.gacs.model.Usuario;

import java.util.Objects;

/** Mantém o usuário autenticado durante a execução da aplicação. */
public final class SessaoUsuario {
    private static Usuario usuarioAutenticado;

    private SessaoUsuario() { }

    public static void iniciar(Usuario usuario) {
        Objects.requireNonNull(usuario, "O usuário autenticado não pode ser nulo.");

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException("Não é possível iniciar uma sessão com usuário inativo.");
        }

        usuarioAutenticado = usuario;
    }

    public static boolean estaAutenticado() {
        return usuarioAutenticado != null;
    }

    public static Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public static void encerrar() {
        usuarioAutenticado = null;
    }
}
