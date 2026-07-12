package br.uel.gacs.application;

import br.uel.gacs.model.Usuario;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Compõe a tela principal exibida depois da autenticação. */
public final class TelaPrincipal {
    private final Window janela;
    private final Runnable acaoSair;

    public TelaPrincipal(Window janela, Runnable acaoSair) {
        if (janela == null || acaoSair == null) {
            throw new IllegalArgumentException("A janela e a ação de saída devem ser informadas.");
        }
        this.janela = janela;
        this.acaoSair = acaoSair;
    }

    /** Cria todo o conteúdo visual da tela principal. */
    public Parent criar() {
        Usuario usuario = SessaoUsuario.exigirUsuarioLogado();

        BorderPane raiz = new BorderPane();
        raiz.setTop(new MenuPrincipal(
                janela,
                acaoSair,
                () -> new TelaCadastroUsuarios(janela).exibir()).criar());
        raiz.setCenter(criarAreaCentral());
        raiz.setBottom(criarBarraEstado(usuario));
        raiz.setStyle("-fx-background-color: #f7f9fb;");
        return raiz;
    }

    private VBox criarAreaCentral() {
        Label titulo = new Label("GACS");
        titulo.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #244766;");

        Label nome = new Label("Gerenciador para Análise e Caracterização de Componentes Semicondutores");
        nome.setStyle("-fx-font-size: 16px; -fx-text-fill: #4b6275;");
        nome.setWrapText(true);

        Label orientacao = new Label("Escolha uma opção no menu ou utilize um dos atalhos acima.");
        orientacao.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7780;");

        VBox area = new VBox(10, titulo, nome, orientacao);
        area.setAlignment(Pos.CENTER);
        area.setPadding(new Insets(35));
        return area;
    }

    private Label criarBarraEstado(Usuario usuario) {
        Label barra = new Label("Usuário conectado: " + usuario.getNome());
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setPadding(new Insets(8, 14, 8, 14));
        barra.setStyle("-fx-background-color: #244766; -fx-text-fill: white;");
        return barra;
    }
}
