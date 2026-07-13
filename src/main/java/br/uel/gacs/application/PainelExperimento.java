package br.uel.gacs.application;

import br.uel.gacs.controller.ExperimentoCtlr;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.util.FormatadorNumero;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private final PlanilhaExperimento planilha;
    private boolean alterado;
    private Runnable acaoMarcarAlterado = () -> { };
    private String textoInicialEdicao;
    private TableView<Integer> tabela;

    public PainelExperimento(Window janela, ExperimentoCtlr controller, Experimento experimento,
                             String proprietario, PlanilhaExperimento planilha,
                             Runnable acaoFechar) {
        this.janela = janela; this.controller = controller; this.experimento = experimento;
        this.proprietario = proprietario;
        this.planilha = planilha == null ? new PlanilhaExperimento() : planilha;
        this.acaoFechar = acaoFechar;
        this.alterado = experimento.getId() == null;
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

        tabela = criarTabela();
        Label estado = new Label(experimento.getId() == null ? "Novo — ainda não salvo" : "Experimento salvo");
        Button salvar = new Button("Salvar Experimento");
        Button fechar = new Button("Fechar Experimento");
        Region espaco = new Region(); HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox botoes = new HBox(10, estado, espaco, salvar, fechar);

        Runnable marcar = () -> { alterado = true; estado.setText("Alterações não salvas"); };
        acaoMarcarAlterado = marcar;
        nome.textProperty().addListener((o,a,n)->marcar.run());
        data.textProperty().addListener((o,a,n)->marcar.run());
        observacoes.textProperty().addListener((o,a,n)->marcar.run());

        salvar.setOnAction(e -> {
            try {
                experimento.setNomeExperimento(nome.getText().trim());
                experimento.setDataExperimento(LocalDateTime.parse(data.getText().trim(), DATA));
                experimento.setObservacoes(observacoes.getText());
                controller.salvarCompleto(experimento, planilha);
                alterado = false; estado.setText("Experimento salvo");
            } catch (DateTimeParseException x) { erro("Use o formato de data dd/mm/aaaa hh:mm.");
            } catch (IllegalArgumentException x) { erro(x.getMessage());
            } catch (SQLException x) { erro("Não foi possível salvar o experimento."); }
        });
        fechar.setOnAction(e -> { if (confirmarFechamento()) acaoFechar.run(); });

        HBox ferramentas = criarFerramentasPlanilha(tabela);
        VBox raiz = new VBox(12, formulario, ferramentas, tabela, botoes); raiz.setPadding(new Insets(16));
        VBox.setVgrow(tabela, Priority.ALWAYS); return raiz;
    }

    private TableView<Integer> criarTabela() {
        TableView<Integer> tabela = new TableView<>();
        tabela.setEditable(true);
        tabela.getSelectionModel().setCellSelectionEnabled(true);
        tabela.setPlaceholder(new Label("A planilha do experimento está vazia."));
        atualizarLinhas(tabela);
        for (int i=0;i<planilha.getQuantidadeColunas();i++) {
            adicionarColunaVisual(tabela, i);
        }
        configurarTeclado(tabela);
        if (!tabela.getItems().isEmpty() && !tabela.getColumns().isEmpty()) {
            Platform.runLater(() -> selecionarEEditar(tabela,0,0));
        }
        return tabela;
    }

    private HBox criarFerramentasPlanilha(TableView<Integer> tabela) {
        Button adicionarLinha = new Button("Adicionar linha");
        Button adicionarColuna = new Button("Adicionar coluna");

        adicionarLinha.setOnAction(e -> {
            try { planilha.adicionarMedidaVazia(); atualizarLinhas(tabela); acaoMarcarAlterado.run(); }
            catch (IllegalArgumentException | IllegalStateException x) { erro(x.getMessage()); }
        });
        adicionarColuna.setOnAction(e -> {
            adicionarColunaParaDigitacao();
        });
        return new HBox(8, adicionarLinha, adicionarColuna);
    }

    /** Acrescenta uma coluna à planilha aberta sem descartar os dados existentes. */
    public void adicionarColunaParaDigitacao() {
        if (tabela == null) return;
        TextInputDialog dialogo = new TextInputDialog(); dialogo.initOwner(janela);
        dialogo.setTitle("Nova coluna"); dialogo.setHeaderText("Informe o nome da nova coluna.");
        dialogo.showAndWait().ifPresent(nome -> {
            try {
                planilha.adicionarColuna(nome);
                reconstruirColunas(tabela);
                acaoMarcarAlterado.run();
                int novaColuna=planilha.getQuantidadeColunas()-1;
                if(planilha.getQuantidadeMedidas()==0){planilha.adicionarMedidaVazia();atualizarLinhas(tabela);}
                Platform.runLater(()->selecionarEEditar(tabela,0,novaColuna));
            } catch (IllegalArgumentException | IllegalStateException x) { erro(x.getMessage()); }
        });
    }

    /** Acrescenta um bloco colado ou importado à direita da grade atual. */
    public void acrescentarPlanilha(PlanilhaExperimento novaPlanilha) {
        int primeiraNovaColuna=planilha.getQuantidadeColunas();
        try {
            planilha.acrescentarColunas(novaPlanilha);
            reconstruirColunas(tabela);
            acaoMarcarAlterado.run();
            if(planilha.getQuantidadeMedidas()>0)
                Platform.runLater(()->selecionarEEditar(tabela,0,primeiraNovaColuna));
        } catch(IllegalArgumentException | IllegalStateException e) { erro(e.getMessage()); }
    }

    private void reconstruirColunas(TableView<Integer> tabela) {
        tabela.getColumns().clear();
        for (int i=0;i<planilha.getQuantidadeColunas();i++) adicionarColunaVisual(tabela,i);
        atualizarLinhas(tabela);
    }

    private void adicionarColunaVisual(TableView<Integer> tabela, int indice) {
        TableColumn<Integer,Double> coluna = new TableColumn<>();
        coluna.setGraphic(criarCabecalhoColuna(tabela,indice));
        coluna.setReorderable(false);
        coluna.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(planilha.getValor(c.getValue(),indice)));
        coluna.setCellFactory(c -> new CelulaNumerica(tabela,indice));
        coluna.setPrefWidth(150);
        tabela.getColumns().add(coluna);
    }

    private void atualizarLinhas(TableView<Integer> tabela) {
        List<Integer> indices = java.util.stream.IntStream.range(0,planilha.getQuantidadeMedidas()).boxed().toList();
        tabela.setItems(FXCollections.observableArrayList(indices));
    }

    private VBox criarCabecalhoColuna(TableView<Integer> tabela, int coluna) {
        Label rotulo = new Label(planilha.getRotuloColuna(coluna));
        rotulo.setMaxWidth(Double.MAX_VALUE);
        rotulo.setAlignment(javafx.geometry.Pos.CENTER);
        rotulo.setStyle("-fx-font-weight: bold;");

        TextField nome = new TextField(planilha.getNomeColuna(coluna));
        nome.setPromptText("Nome da coluna");
        nome.setMaxWidth(Double.MAX_VALUE);
        nome.setOnAction(e -> {
            tabela.requestFocus();
            if (!tabela.getItems().isEmpty()) selecionarEEditar(tabela,0,coluna);
        });
        nome.focusedProperty().addListener((o, tinhaFoco, temFoco) -> {
            if (!temFoco) {
                try {
                    planilha.setNomeColuna(coluna,nome.getText());
                    nome.setText(planilha.getNomeColuna(coluna));
                    acaoMarcarAlterado.run();
                } catch (IllegalArgumentException e) {
                    erro(e.getMessage());
                    nome.setText(planilha.getNomeColuna(coluna));
                }
            }
        });
        return new VBox(3,rotulo,nome);
    }

    private void configurarTeclado(TableView<Integer> tabela) {
        tabela.addEventFilter(KeyEvent.KEY_PRESSED,e->{
            if(e.getTarget() instanceof TextInputControl)return;
            TablePosition<Integer,?> p=celulaSelecionada(tabela); if(p==null)return;
            if(e.getCode()==KeyCode.DELETE&&tabela.getEditingCell()==null){planilha.setValor(p.getRow(),p.getColumn(),null);acaoMarcarAlterado.run();tabela.refresh();e.consume();}
            else if(e.getCode()==KeyCode.ENTER&&tabela.getEditingCell()==null){selecionarEEditar(tabela,p.getRow(),p.getColumn());e.consume();}
        });
        tabela.addEventFilter(KeyEvent.KEY_TYPED,e->{
            if(e.getTarget() instanceof TextInputControl)return;
            if(tabela.getEditingCell()!=null||e.isControlDown()||e.isAltDown()||e.isMetaDown())return;
            String c=e.getCharacter(); if(c==null||c.isBlank()||"\r\n\t".contains(c))return;
            TablePosition<Integer,?> p=celulaSelecionada(tabela); if(p==null)return;
            textoInicialEdicao=c; selecionarEEditar(tabela,p.getRow(),p.getColumn()); e.consume();
        });
    }

    private TablePosition<Integer,?> celulaSelecionada(TableView<Integer> tabela){return tabela.getSelectionModel().getSelectedCells().stream().findFirst().orElse(null);}

    private void selecionarEEditar(TableView<Integer> tabela,int linha,int coluna){
        if(linha<0||coluna<0||coluna>=tabela.getColumns().size())return;
        if(linha>=planilha.getQuantidadeMedidas()){try{planilha.adicionarMedidaVazia();atualizarLinhas(tabela);}catch(RuntimeException e){erro(e.getMessage());return;}}
        tabela.getSelectionModel().clearAndSelect(linha,tabela.getColumns().get(coluna));tabela.edit(linha,tabela.getColumns().get(coluna));
    }

    private void moverDepoisDaEdicao(TableView<Integer> tabela,int linha,int coluna,KeyEvent evento){
        if(evento.getCode()==KeyCode.ENTER)linha+=evento.isShiftDown()?-1:1;
        else if(evento.getCode()==KeyCode.TAB){coluna+=evento.isShiftDown()?-1:1;if(coluna>=planilha.getQuantidadeColunas()){coluna=0;linha++;}else if(coluna<0){coluna=planilha.getQuantidadeColunas()-1;linha--;}}
        int destinoLinha=linha,destinoColuna=coluna;Platform.runLater(()->selecionarEEditar(tabela,destinoLinha,destinoColuna));
    }

    private final class CelulaNumerica extends TableCell<Integer,Double>{
        private final TableView<Integer> tabela;private final int coluna;private final TextField editor=new TextField();
        private CelulaNumerica(TableView<Integer> tabela,int coluna){this.tabela=tabela;this.coluna=coluna;editor.setOnKeyPressed(e->{if(e.getCode()==KeyCode.ENTER||e.getCode()==KeyCode.TAB){if(confirmar()){moverDepoisDaEdicao(tabela,getIndex(),coluna,e);}e.consume();}});editor.focusedProperty().addListener((o,a,f)->{if(!f&&isEditing())confirmar();});}
        @Override public void startEdit(){if(isEmpty())return;super.startEdit();String inicial=textoInicialEdicao;textoInicialEdicao=null;editor.setText(inicial!=null?inicial:FormatadorNumero.formatar(getItem()));setText(null);setGraphic(editor);Platform.runLater(()->{editor.requestFocus();if(inicial==null)editor.selectAll();else editor.positionCaret(editor.getText().length());});}
        @Override public void cancelEdit(){super.cancelEdit();setGraphic(null);setText(FormatadorNumero.formatar(getItem()));}
        @Override protected void updateItem(Double valor,boolean vazio){super.updateItem(valor,vazio);if(vazio){setText(null);setGraphic(null);}else if(!isEditing()){setGraphic(null);setText(FormatadorNumero.formatar(valor));}}
        private boolean confirmar(){try{Double valor=editor.getText().isBlank()?null:FormatadorNumero.converter(editor.getText());planilha.setValor(getIndex(),coluna,valor);commitEdit(valor);acaoMarcarAlterado.run();tabela.refresh();return true;}catch(IllegalArgumentException e){erro(e.getMessage());Platform.runLater(()->{editor.requestFocus();editor.selectAll();});return false;}}
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
