package br.uel.gacs.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Monta os menus e os atalhos principais do GACS. */
public final class MenuPrincipal {
    private final Window janela;
    private final Runnable acaoSair;
    private final Runnable acaoCadastroUsuarios;

    public MenuPrincipal(Window janela, Runnable acaoSair, Runnable acaoCadastroUsuarios) {
        if (janela == null || acaoSair == null || acaoCadastroUsuarios == null) {
            throw new IllegalArgumentException("A janela e as ações devem ser informadas.");
        }
        this.janela = janela;
        this.acaoSair = acaoSair;
        this.acaoCadastroUsuarios = acaoCadastroUsuarios;
    }

    /** Cria a região superior composta pela barra de menus e pelos atalhos. */
    public VBox criar() {
        return new VBox(criarBarraMenus(), criarFaixaAtalhos());
    }

    private MenuBar criarBarraMenus() {
        Menu menuArquivo = new Menu("Arquivo");
        menuArquivo.getItems().addAll(
                criarItemProvisorio("Novo Experimento"),
                criarItemProvisorio("Carregar Experimento"),
                criarItemProvisorio("Novo Gráfico"),
                new SeparatorMenuItem(),
                criarItem("Sair", acaoSair));

        Menu menuRelatorios = new Menu("Relatórios");
        menuRelatorios.getItems().addAll(
                criarItemProvisorio("Relatório de Experimento"),
                criarItemProvisorio("Relatório de Gráfico"));

        Menu menuManutencao = new Menu("Manutenção");
        menuManutencao.getItems().add(criarItem("Cadastro de Usuários", acaoCadastroUsuarios));
        menuManutencao.setVisible(Autorizador.usuarioAtualEhAdministrador());

        return new MenuBar(menuArquivo, menuRelatorios, menuManutencao);
    }

    private HBox criarFaixaAtalhos() {
        Button novoExperimento = criarBotaoAtalho("Novo\nExperimento");
        Button carregarExperimento = criarBotaoAtalho("Carregar\nExperimento");
        Button novoGrafico = criarBotaoAtalho("Novo\nGráfico");

        novoExperimento.setOnAction(evento -> informarFuncionalidadeFutura("Novo Experimento"));
        carregarExperimento.setOnAction(evento -> informarFuncionalidadeFutura("Carregar Experimento"));
        novoGrafico.setOnAction(evento -> informarFuncionalidadeFutura("Novo Gráfico"));

        HBox faixa = new HBox(12, novoExperimento, carregarExperimento, novoGrafico);
        faixa.setAlignment(Pos.CENTER_LEFT);
        faixa.setPadding(new Insets(14, 18, 14, 18));
        faixa.setStyle("-fx-background-color: #eaf1f8; -fx-border-color: #c5d3e0; "
                + "-fx-border-width: 0 0 1 0;");
        return faixa;
    }

    private Button criarBotaoAtalho(String texto) {
        Button botao = new Button(texto);
        botao.setPrefSize(150, 64);
        botao.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-color: #ffffff; -fx-border-color: #8ba8c2; "
                + "-fx-border-radius: 5; -fx-background-radius: 5;");
        return botao;
    }

    private MenuItem criarItemProvisorio(String texto) {
        return criarItem(texto, () -> informarFuncionalidadeFutura(texto));
    }

    private MenuItem criarItem(String texto, Runnable acao) {
        MenuItem item = new MenuItem(texto);
        item.setOnAction(evento -> acao.run());
        return item;
    }

    private void informarFuncionalidadeFutura(String funcionalidade) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(janela);
        alerta.setTitle("GACS");
        alerta.setHeaderText(funcionalidade);
        alerta.setContentText("Esta funcionalidade será implementada em uma próxima etapa.");
        alerta.showAndWait();
    }
}
