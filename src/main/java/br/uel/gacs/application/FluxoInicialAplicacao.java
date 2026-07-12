package br.uel.gacs.application;

import br.uel.gacs.controller.LoginCtlr;
import br.uel.gacs.controller.UsuarioCtlr;
import br.uel.gacs.dao.CriadorBancoDados;
import br.uel.gacs.model.PerfilUsuario;
import br.uel.gacs.model.Usuario;

import java.sql.SQLException;
import java.util.Optional;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Controla o fluxo visual de inicialização, cadastro inicial e login do GACS. */
public final class FluxoInicialAplicacao {
    private static final double LARGURA_LOGIN = 400;
    private static final double ALTURA_LOGIN = 200;

    private final Stage palcoPrincipal;
    private final UsuarioCtlr usuarioCtlr = new UsuarioCtlr();
    private final LoginCtlr loginCtlr = new LoginCtlr();

    public FluxoInicialAplicacao(Stage palcoPrincipal) {
        if (palcoPrincipal == null) {
            throw new IllegalArgumentException("O palco principal deve ser informado.");
        }
        this.palcoPrincipal = palcoPrincipal;
    }

    /** Inicia a aplicação mostrando imediatamente a janela de processamento. */
    public void iniciar() {
        exibirTelaProcessamento();
        inicializarBancoEmSegundoPlano();
    }

    private void exibirTelaProcessamento() {
        Label titulo = new Label("GACS");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label mensagem = new Label("Aguarde. Verificando e preparando o banco de dados...");
        mensagem.setWrapText(true);

        ProgressIndicator progresso = new ProgressIndicator();
        progresso.setPrefSize(45, 45);

        VBox conteudo = new VBox(12, titulo, progresso, mensagem);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPadding(new Insets(20));

        configurarPalco("Inicializando o GACS", conteudo, LARGURA_LOGIN, ALTURA_LOGIN);
    }

    private void inicializarBancoEmSegundoPlano() {
        Task<Boolean> tarefa = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return CriadorBancoDados.criarSeNaoExistir();
            }
        };

        tarefa.setOnSucceeded(evento -> verificarPrimeiroUsuario());
        tarefa.setOnFailed(evento -> {
            Throwable causa = tarefa.getException();
            exibirErroFatal(
                    "Não foi possível preparar o banco de dados.",
                    causa == null ? null : causa.getMessage());
        });

        Thread thread = new Thread(tarefa, "inicializacao-banco-gacs");
        thread.setDaemon(true);
        thread.start();
    }

    private void verificarPrimeiroUsuario() {
        Task<Boolean> tarefa = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return usuarioCtlr.possuiUsuarios();
            }
        };

        tarefa.setOnSucceeded(evento -> {
            if (tarefa.getValue()) {
                exibirTelaLogin();
            } else {
                exibirTelaCadastroInicial();
            }
        });
        tarefa.setOnFailed(evento -> exibirErroFatal(
                "Não foi possível verificar os usuários cadastrados.",
                tarefa.getException().getMessage()));

        Thread thread = new Thread(tarefa, "verificacao-usuario-gacs");
        thread.setDaemon(true);
        thread.start();
    }

    private void exibirTelaCadastroInicial() {
        Label orientacao = new Label("Cadastre o administrador inicial do GACS");
        orientacao.setStyle("-fx-font-weight: bold;");

        TextField campoNome = new TextField();
        TextField campoEmail = new TextField();
        PasswordField campoSenha = new PasswordField();
        PasswordField campoConfirmacao = new PasswordField();
        Label mensagem = new Label();
        mensagem.setStyle("-fx-text-fill: #b00020;");
        mensagem.setWrapText(true);

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(9);
        formulario.addRow(0, new Label("Nome:"), campoNome);
        formulario.addRow(1, new Label("E-mail:"), campoEmail);
        formulario.addRow(2, new Label("Senha:"), campoSenha);
        formulario.addRow(3, new Label("Confirmar:"), campoConfirmacao);
        campoNome.setPrefWidth(245);

        Button botaoCadastrar = new Button("Cadastrar e entrar");
        botaoCadastrar.setDefaultButton(true);
        botaoCadastrar.setOnAction(evento -> {
            if (!campoSenha.getText().equals(campoConfirmacao.getText())) {
                mensagem.setText("A confirmação da senha não corresponde.");
                return;
            }

            try {
                Usuario usuario = usuarioCtlr.cadastrar(
                        campoNome.getText(),
                        campoEmail.getText(),
                        campoSenha.getText(),
                        PerfilUsuario.ADMINISTRADOR);
                SessaoUsuario.iniciar(usuario);
                exibirTelaPrincipal();
            } catch (IllegalArgumentException excecao) {
                mensagem.setText(excecao.getMessage());
            } catch (SQLException excecao) {
                mensagem.setText("Não foi possível cadastrar o usuário.");
            }
        });

        VBox conteudo = new VBox(12, orientacao, formulario, botaoCadastrar, mensagem);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPadding(new Insets(18));

        configurarPalco("Primeiro acesso ao GACS", conteudo, 400, 300);
        campoNome.requestFocus();
    }

    private void exibirTelaLogin() {
        Label titulo = new Label("Entrar no GACS");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField campoEmail = new TextField();
        campoEmail.setPromptText("E-mail");
        PasswordField campoSenha = new PasswordField();
        campoSenha.setPromptText("Senha");
        Label mensagem = new Label();
        mensagem.setStyle("-fx-text-fill: #b00020;");

        Button botaoEntrar = new Button("Entrar");
        botaoEntrar.setDefaultButton(true);
        botaoEntrar.setOnAction(evento -> {
            try {
                Optional<Usuario> usuario = loginCtlr.autenticar(
                        campoEmail.getText(), campoSenha.getText());

                if (usuario.isPresent()) {
                    exibirTelaPrincipal();
                } else {
                    mensagem.setText("E-mail ou senha inválidos, ou usuário inativo.");
                }
            } catch (SQLException excecao) {
                mensagem.setText("Não foi possível acessar o banco de dados.");
            }
        });

        VBox conteudo = new VBox(9, titulo, campoEmail, campoSenha, botaoEntrar, mensagem);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPadding(new Insets(20, 55, 20, 55));

        configurarPalco("Login - GACS", conteudo, LARGURA_LOGIN, ALTURA_LOGIN);
        campoEmail.requestFocus();
    }

    private void exibirTelaPrincipal() {
        TelaPrincipal telaPrincipal = new TelaPrincipal(palcoPrincipal, () -> {
            loginCtlr.sair();
            exibirTelaLogin();
        });
        configurarPalco("GACS", telaPrincipal.criar(), 1280, 800);
        palcoPrincipal.setMinWidth(1050);
        palcoPrincipal.setMinHeight(700);
        palcoPrincipal.setResizable(true);
    }

    private void configurarPalco(String titulo, javafx.scene.Parent conteudo,
                                 double largura, double altura) {
        palcoPrincipal.setTitle(titulo);
        palcoPrincipal.setScene(new Scene(conteudo, largura, altura));
        palcoPrincipal.setResizable(false);
        palcoPrincipal.centerOnScreen();
        palcoPrincipal.show();
    }

    private void exibirErroFatal(String cabecalho, String detalhe) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(palcoPrincipal);
        alerta.setTitle("Erro ao iniciar o GACS");
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(detalhe == null || detalhe.isBlank()
                ? "Verifique o MySQL e as configurações de conexão."
                : detalhe);
        alerta.showAndWait();
        palcoPrincipal.close();
    }
}
