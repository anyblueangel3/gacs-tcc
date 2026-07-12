package br.uel.gacs.application;

import br.uel.gacs.model.PerfilUsuario;
import br.uel.gacs.model.Usuario;

/** Centraliza as verificações de autorização baseadas na sessão atual. */
public final class Autorizador {
    private Autorizador() { }

    public static boolean usuarioAtualEhAdministrador() {
        return SessaoUsuario.getUsuarioLogado()
                .map(Usuario::getPerfil)
                .filter(PerfilUsuario.ADMINISTRADOR::equals)
                .isPresent();
    }

    public static void exigirAdministrador() {
        if (!usuarioAtualEhAdministrador()) {
            throw new SecurityException("Esta operação é permitida somente a administradores.");
        }
    }
}
