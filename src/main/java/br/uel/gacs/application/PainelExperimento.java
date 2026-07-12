package br.uel.gacs.application;

import br.uel.gacs.controller.ExperimentoCtlr;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.Usuario;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

/** Área de trabalho de um experimento, incorporada à janela principal. */
public final class PainelExperimento {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Window janela;
    private final ExperimentoCtlr controller;
    private final Experimento experimento;
    private final String proprietario;
    private final Runnable acaoFechar;
    private final List<List<String>> dadosColados;
    private boolean alterado;

    public PainelExperimento(Window janela, ExperimentoCtlr controller, Experimento experimento,
                             String proprietario, List<List<String>> dadosColados,
                             Runnable acaoFechar) {
        this.janela = janela; this.controller = controller; this.experimento = experimento;
        this.proprietario = proprietario; this.dadosColados = dadosColados; this.acaoFechar = acaoFechar;
    }

    public Parent criar() {
        TextField nome = new TextField(experimento.getNomeExperimento());
        TextField data = new TextField(experimento.getDataExperimento().format(DATA));
        TextArea observacoes = new TextArea(experimento.getObservacoes());
        observacoes.setWrapText(true); observacoes.setPrefRowCount(4);
        observacoes.setPromptText("Observações, procedimentos ou relatório do experimento.");

        GridPane formulario = new GridPane(); formulario.setHgap(10); formulario.setVgap(8);
        formulario.addRow(0, new Label("Nome:"), nome, new Label("Data e hora:"), data);
        formulario.addRow(1, new Label("Responsável:"), new Label(proprietario));
        formulario.add(new Label("Observações:"), 0, 2); formulario.add(observacoes, 1, 2, 3, 1);
        GridPane.setHgrow(nome, Priority.ALWAYS); GridPane.setHgrow(observacoes, Priority.ALWAYS);

        TableView<List<String>> tabela = criarTabela();
        Label estado = new Label(experimento.getId() == null ? "Novo — ainda não salvo" : "Experimento salvo");
        Button salvar = new Button("Salvar Experimento");
        Button fechar = new Button("Fechar Experimento");
        Region espaco = new Region(); HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox botoes = new HBox(10, estado, espaco, salvar, fechar);

        Runnable marcar = () -> { alterado = true; estado.setText("Alterações não salvas"); };
        nome.textProperty().addListener((o,a,n)->marcar.run());
        data.textProperty().addListener((o,a,n)->marcar.run());
        observacoes.textProperty().addListener((o,a,n)->marcar.run());

        salvar.setOnAction(e -> {
            try {
                experimento.setNomeExperimento(nome.getText().trim());
                experimento.setDataExperimento(LocalDateTime.parse(data.getText().trim(), DATA));
                experimento.setObservacoes(observacoes.getText());
                if (experimento.getId() == null) controller.salvarNovo(experimento);
                else controller.salvarAlteracoes(experimento);
                alterado = false; estado.setText("Experimento salvo");
            } catch (DateTimeParseException x) { erro("Use o formato de data dd/mm/aaaa hh:mm.");
            } catch (IllegalArgumentException x) { erro(x.getMessage());
            } catch (SQLException x) { erro("Não foi possível salvar o experimento."); }
        });
        fechar.setOnAction(e -> { if (confirmarFechamento()) acaoFechar.run(); });

        VBox raiz = new VBox(12, formulario, tabela, botoes); raiz.setPadding(new Insets(16));
        VBox.setVgrow(tabela, Priority.ALWAYS); return raiz;
    }

    private TableView<List<String>> criarTabela() {
        TableView<List<String>> tabela = new TableView<>();
        tabela.setPlaceholder(new Label("A planilha do experimento está vazia."));
        if (dadosColados == null || dadosColados.isEmpty()) return tabela;
        int colunas = dadosColados.getFirst().size();
        for (int i=0;i<colunas;i++) {
            final int indice=i;
            TableColumn<List<String>,String> coluna = new TableColumn<>(rotulo(i));
            coluna.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().get(indice)));
            coluna.setPrefWidth(130); tabela.getColumns().add(coluna);
        }
        tabela.setItems(FXCollections.observableArrayList(dadosColados)); return tabela;
    }

    private String rotulo(int indice) {
        StringBuilder s=new StringBuilder(); int n=indice+1;
        while(n>0){n--;s.insert(0,(char)('A'+n%26));n/=26;} return s.toString();
    }
    private boolean confirmarFechamento() {
        if (!alterado) return true;
        Alert a=new Alert(Alert.AlertType.CONFIRMATION);a.initOwner(janela);
        a.setTitle("Alterações não salvas");a.setHeaderText("Deseja descartar as alterações não salvas?");
        a.setContentText("Esta ação não poderá ser desfeita.");
        return a.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
    private void erro(String texto){Alert a=new Alert(Alert.AlertType.ERROR);a.initOwner(janela);a.setTitle("GACS");a.setHeaderText("Não foi possível concluir a operação.");a.setContentText(texto);a.showAndWait();}
}
