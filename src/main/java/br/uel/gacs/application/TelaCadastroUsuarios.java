package br.uel.gacs.application;

import br.uel.gacs.controller.UsuarioCtlr;
import br.uel.gacs.model.PerfilUsuario;
import br.uel.gacs.model.Usuario;

import java.sql.SQLException;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Tela administrativa para cadastro e manutenção dos usuários. */
public final class TelaCadastroUsuarios {
    private final Window proprietario;
    private final UsuarioCtlr usuarioCtlr = new UsuarioCtlr();
    private final TableView<Usuario> tabela = new TableView<>();
    private Stage palco;

    public TelaCadastroUsuarios(Window proprietario) {
        this.proprietario = proprietario;
    }

    public void exibir() {
        Autorizador.exigirAdministrador();
        palco = new Stage();
        palco.initOwner(proprietario);
        palco.initModality(Modality.WINDOW_MODAL);
        palco.setTitle("Cadastro de Usuários - GACS");

        configurarTabela();
        BorderPane raiz = new BorderPane(tabela);
        raiz.setTop(criarCabecalho());
        raiz.setBottom(criarBotoes());
        raiz.setPadding(new Insets(16));

        palco.setScene(new Scene(raiz, 920, 520));
        palco.setMinWidth(780);
        palco.setMinHeight(440);
        carregarUsuarios();
        palco.showAndWait();
    }

    private Label criarCabecalho() {
        Label titulo = new Label("Usuários cadastrados");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #244766;");
        BorderPane.setMargin(titulo, new Insets(0, 0, 14, 0));
        return titulo;
    }

    private void configurarTabela() {
        TableColumn<Usuario, Number> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(dado -> new SimpleLongProperty(dado.getValue().getId()));
        colunaId.setPrefWidth(65);

        TableColumn<Usuario, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(dado -> new SimpleStringProperty(dado.getValue().getNome()));
        colunaNome.setPrefWidth(240);

        TableColumn<Usuario, String> colunaEmail = new TableColumn<>("E-mail");
        colunaEmail.setCellValueFactory(dado -> new SimpleStringProperty(dado.getValue().getEmail()));
        colunaEmail.setPrefWidth(280);

        TableColumn<Usuario, String> colunaPerfil = new TableColumn<>("Perfil");
        colunaPerfil.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getPerfil().name()));
        colunaPerfil.setPrefWidth(145);

        TableColumn<Usuario, String> colunaSituacao = new TableColumn<>("Situação");
        colunaSituacao.setCellValueFactory(dado -> new SimpleStringProperty(
                Boolean.TRUE.equals(dado.getValue().getAtivo()) ? "Ativo" : "Inativo"));
        colunaSituacao.setPrefWidth(100);

        tabela.getColumns().addAll(
                colunaId, colunaNome, colunaEmail, colunaPerfil, colunaSituacao);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum usuário cadastrado."));
    }

    private HBox criarBotoes() {
        Button novo = new Button("Novo");
        Button editar = new Button("Editar");
        Button senha = new Button("Redefinir senha");
        Button situacao = new Button("Ativar/Inativar");
        Button fechar = new Button("Fechar");

        novo.setOnAction(evento -> cadastrar());
        editar.setOnAction(evento -> editar());
        senha.setOnAction(evento -> redefinirSenha());
        situacao.setOnAction(evento -> alternarSituacao());
        fechar.setOnAction(evento -> palco.close());

        HBox botoes = new HBox(10, novo, editar, senha, situacao, fechar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(14, 0, 0, 0));
        return botoes;
    }

    private void carregarUsuarios() {
        try {
            tabela.setItems(FXCollections.observableArrayList(usuarioCtlr.listarTodos()));
        } catch (SQLException excecao) {
            exibirErro("Não foi possível carregar os usuários.");
        }
    }

    private void cadastrar() {
        FormularioUsuario formulario = new FormularioUsuario(null);
        if (!formulario.exibir("Novo usuário", true)) {
            return;
        }

        try {
            usuarioCtlr.cadastrar(
                    formulario.nome(), formulario.email(), formulario.senha(), formulario.perfil());
            carregarUsuarios();
        } catch (IllegalArgumentException | SecurityException excecao) {
            exibirErro(excecao.getMessage());
        } catch (SQLException excecao) {
            exibirErro("Não foi possível cadastrar o usuário.");
        }
    }

    private void editar() {
        Usuario usuario = exigirSelecionado();
        if (usuario == null) {
            return;
        }

        FormularioUsuario formulario = new FormularioUsuario(usuario);
        if (!formulario.exibir("Editar usuário", false)) {
            return;
        }

        Usuario usuarioAlterado = new Usuario(
                usuario.getId(), formulario.nome(), formulario.email(), usuario.getSenhaHash(),
                formulario.perfil(), usuario.getAtivo(), usuario.getDataCriacao(),
                usuario.getDataUltimaAlteracao());
        try {
            usuarioCtlr.atualizar(usuarioAlterado);
            carregarUsuarios();
        } catch (IllegalArgumentException | SecurityException excecao) {
            exibirErro(excecao.getMessage());
        } catch (SQLException excecao) {
            exibirErro("Não foi possível atualizar o usuário.");
        }
    }

    private void redefinirSenha() {
        Usuario usuario = exigirSelecionado();
        if (usuario == null) {
            return;
        }

        PasswordField senha = new PasswordField();
        PasswordField confirmacao = new PasswordField();
        GridPane campos = criarGrade();
        campos.addRow(0, new Label("Nova senha:"), senha);
        campos.addRow(1, new Label("Confirmar:"), confirmacao);

        Dialog<ButtonType> dialogo = criarDialogo("Redefinir senha", campos);
        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        if (!senha.getText().equals(confirmacao.getText())) {
            exibirErro("A confirmação da senha não corresponde.");
            return;
        }

        try {
            usuarioCtlr.alterarSenha(usuario.getId(), senha.getText());
            exibirInformacao("Senha alterada com sucesso.");
        } catch (IllegalArgumentException | SecurityException excecao) {
            exibirErro(excecao.getMessage());
        } catch (SQLException excecao) {
            exibirErro("Não foi possível alterar a senha.");
        }
    }

    private void alternarSituacao() {
        Usuario usuario = exigirSelecionado();
        if (usuario == null) {
            return;
        }
        boolean novoStatus = !Boolean.TRUE.equals(usuario.getAtivo());
        String operacao = novoStatus ? "ativar" : "inativar";

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.initOwner(palco);
        confirmacao.setTitle("Confirmar alteração");
        confirmacao.setHeaderText("Deseja " + operacao + " o usuário " + usuario.getNome() + "?");
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            usuarioCtlr.alterarStatus(usuario.getId(), novoStatus);
            carregarUsuarios();
        } catch (IllegalArgumentException | SecurityException excecao) {
            exibirErro(excecao.getMessage());
        } catch (SQLException excecao) {
            exibirErro("Não foi possível alterar a situação do usuário.");
        }
    }

    private Usuario exigirSelecionado() {
        Usuario usuario = tabela.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            exibirErro("Selecione um usuário na tabela.");
        }
        return usuario;
    }

    private Dialog<ButtonType> criarDialogo(String titulo, GridPane conteudo) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(palco);
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialogo;
    }

    private GridPane criarGrade() {
        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(10));
        return grade;
    }

    private void exibirErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(palco);
        alerta.setTitle("Cadastro de Usuários");
        alerta.setHeaderText("Não foi possível concluir a operação.");
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void exibirInformacao(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(palco);
        alerta.setTitle("Cadastro de Usuários");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private final class FormularioUsuario {
        private final TextField nome = new TextField();
        private final TextField email = new TextField();
        private final ComboBox<PerfilUsuario> perfil = new ComboBox<>(
                FXCollections.observableArrayList(PerfilUsuario.values()));
        private final PasswordField senha = new PasswordField();
        private final PasswordField confirmacao = new PasswordField();

        private FormularioUsuario(Usuario usuario) {
            if (usuario != null) {
                nome.setText(usuario.getNome());
                email.setText(usuario.getEmail());
                perfil.setValue(usuario.getPerfil());
            } else {
                perfil.setValue(PerfilUsuario.OPERADOR);
            }
        }

        private boolean exibir(String titulo, boolean incluirSenha) {
            GridPane grade = criarGrade();
            grade.addRow(0, new Label("Nome:"), nome);
            grade.addRow(1, new Label("E-mail:"), email);
            grade.addRow(2, new Label("Perfil:"), perfil);
            if (incluirSenha) {
                grade.addRow(3, new Label("Senha:"), senha);
                grade.addRow(4, new Label("Confirmar:"), confirmacao);
            }

            Dialog<ButtonType> dialogo = criarDialogo(titulo, grade);
            if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return false;
            }
            if (incluirSenha && !senha.getText().equals(confirmacao.getText())) {
                exibirErro("A confirmação da senha não corresponde.");
                return false;
            }
            return true;
        }

        private String nome() { return nome.getText(); }
        private String email() { return email.getText(); }
        private PerfilUsuario perfil() { return perfil.getValue(); }
        private String senha() { return senha.getText(); }
    }
}
