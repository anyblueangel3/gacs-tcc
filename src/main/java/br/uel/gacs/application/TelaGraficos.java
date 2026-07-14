package br.uel.gacs.application;

import br.uel.gacs.controller.GraficoCtlr;
import br.uel.gacs.controller.GraficoCtlr.CurvaDoGrafico;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.Grafico;
import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

/**
 * Consulta e mantém os gráficos e as curvas de um experimento.
 *
 * @author Ronaldo Rodrigues Godoi e Chat GPT
 */
public final class TelaGraficos {
    private final Window janelaPai;
    private final Long idExperimento;
    private final GraficoCtlr controller = new GraficoCtlr();
    private final TableView<Grafico> tabelaGraficos = new TableView<>();
    private final TableView<CurvaDoGrafico> tabelaCurvas = new TableView<>();

    public TelaGraficos(Window janelaPai, Long idExperimento) {
        this.janelaPai = janelaPai; this.idExperimento = idExperimento;
    }

    public void exibir() {
        if (idExperimento == null) { erro("Salve o experimento antes de gerenciar seus gráficos."); return; }
        Stage janela = new Stage(); janela.initOwner(janelaPai); janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle("Gráficos e curvas do experimento");
        configurarTabelas();

        Button novoGrafico = new Button("Novo gráfico");
        Button renomearGrafico = new Button("Renomear");
        Button excluirGrafico = new Button("Excluir gráfico");
        Button plotarGrafico = new Button("Plotar gráfico");
        Button adicionarCurva = new Button("Adicionar curva");
        Button alterarCurva = new Button("Alterar curva");
        Button removerCurva = new Button("Remover curva");
        Button fechar = new Button("Fechar");

        boolean podeAlterar;
        try { podeAlterar = controller.podeAlterar(idExperimento); }
        catch (SQLException e) { erro("Não foi possível verificar a permissão para alterar o experimento."); return; }

        novoGrafico.setDisable(!podeAlterar);
        renomearGrafico.disableProperty().bind(tabelaGraficos.getSelectionModel().selectedItemProperty().isNull().or(new javafx.beans.property.SimpleBooleanProperty(!podeAlterar)));
        excluirGrafico.disableProperty().bind(tabelaGraficos.getSelectionModel().selectedItemProperty().isNull().or(new javafx.beans.property.SimpleBooleanProperty(!podeAlterar)));
        plotarGrafico.disableProperty().bind(tabelaGraficos.getSelectionModel().selectedItemProperty().isNull());
        adicionarCurva.disableProperty().bind(tabelaGraficos.getSelectionModel().selectedItemProperty().isNull().or(new javafx.beans.property.SimpleBooleanProperty(!podeAlterar)));
        alterarCurva.disableProperty().bind(tabelaCurvas.getSelectionModel().selectedItemProperty().isNull().or(new javafx.beans.property.SimpleBooleanProperty(!podeAlterar)));
        removerCurva.disableProperty().bind(tabelaCurvas.getSelectionModel().selectedItemProperty().isNull().or(new javafx.beans.property.SimpleBooleanProperty(!podeAlterar)));

        novoGrafico.setOnAction(e -> { if (new TelaNovoGrafico(janela, idExperimento).exibir()) carregarGraficos(); });
        renomearGrafico.setOnAction(e -> renomearGrafico());
        excluirGrafico.setOnAction(e -> excluirGrafico());
        plotarGrafico.setOnAction(e -> plotarGrafico());
        adicionarCurva.setOnAction(e -> editarCurva(null));
        alterarCurva.setOnAction(e -> editarCurva(tabelaCurvas.getSelectionModel().getSelectedItem()));
        removerCurva.setOnAction(e -> removerCurva());
        fechar.setOnAction(e -> janela.close());

        VBox ladoGraficos = painel("Gráficos", tabelaGraficos,
                new HBox(8, novoGrafico, renomearGrafico, excluirGrafico, plotarGrafico));
        VBox ladoCurvas = painel("Curvas do gráfico selecionado", tabelaCurvas,
                new HBox(8, adicionarCurva, alterarCurva, removerCurva));
        SplitPane divisao = new SplitPane(ladoGraficos, ladoCurvas); divisao.setDividerPositions(0.36);
        BorderPane raiz = new BorderPane(divisao); raiz.setPadding(new Insets(14));
        HBox rodape = new HBox(fechar); rodape.setPadding(new Insets(10,0,0,0)); raiz.setBottom(rodape);
        janela.setScene(new Scene(raiz, 1050, 560)); carregarGraficos(); janela.showAndWait();
    }

    private void configurarTabelas() {
        TableColumn<Grafico,String> nomeGrafico = new TableColumn<>("Nome do gráfico");
        nomeGrafico.setCellValueFactory(i -> new ReadOnlyStringWrapper(i.getValue().getNome())); nomeGrafico.setPrefWidth(300);
        tabelaGraficos.getColumns().setAll(nomeGrafico); tabelaGraficos.setPlaceholder(new Label("Nenhum gráfico registrado."));
        tabelaGraficos.getSelectionModel().selectedItemProperty().addListener((o,a,n) -> carregarCurvas(n));

        TableColumn<CurvaDoGrafico,Integer> ordem = new TableColumn<>("Ordem");
        ordem.setCellValueFactory(i -> new ReadOnlyObjectWrapper<>(i.getValue().numero())); ordem.setPrefWidth(70);
        TableColumn<CurvaDoGrafico,String> nome = new TableColumn<>("Curva");
        nome.setCellValueFactory(i -> new ReadOnlyStringWrapper(i.getValue().curva().getNome())); nome.setPrefWidth(220);
        TableColumn<CurvaDoGrafico,String> x = new TableColumn<>("Eixo X");
        x.setCellValueFactory(i -> new ReadOnlyStringWrapper(descrever(i.getValue().colunaX()))); x.setPrefWidth(190);
        TableColumn<CurvaDoGrafico,String> y = new TableColumn<>("Eixo Y");
        y.setCellValueFactory(i -> new ReadOnlyStringWrapper(descrever(i.getValue().colunaY()))); y.setPrefWidth(190);
        tabelaCurvas.getColumns().setAll(ordem,nome,x,y); tabelaCurvas.setPlaceholder(new Label("Selecione um gráfico."));
    }

    private VBox painel(String titulo, TableView<?> tabela, HBox botoes) {
        Label label = new Label(titulo); label.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        VBox painel = new VBox(10,label,tabela,botoes); painel.setPadding(new Insets(8)); VBox.setVgrow(tabela,Priority.ALWAYS); return painel;
    }

    private void carregarGraficos() {
        try {
            Long selecionado = tabelaGraficos.getSelectionModel().getSelectedItem() == null ? null
                    : tabelaGraficos.getSelectionModel().getSelectedItem().getId();
            List<Grafico> itens = controller.listarGraficos(idExperimento);
            tabelaGraficos.setItems(FXCollections.observableArrayList(itens));
            Grafico selecionar = itens.stream().filter(g -> g.getId().equals(selecionado)).findFirst()
                    .orElse(itens.isEmpty() ? null : itens.get(0));
            tabelaGraficos.getSelectionModel().select(selecionar);
            if (selecionar == null) tabelaCurvas.getItems().clear();
        } catch (SQLException e) { erro("Não foi possível carregar os gráficos do experimento."); }
    }

    private void carregarCurvas(Grafico grafico) {
        try { tabelaCurvas.setItems(FXCollections.observableArrayList(controller.listarCurvas(grafico))); }
        catch (SQLException e) { erro("Não foi possível carregar as curvas do gráfico."); }
    }

    private void renomearGrafico() {
        Grafico grafico = tabelaGraficos.getSelectionModel().getSelectedItem();
        TextInputDialog d = new TextInputDialog(grafico.getNome()); d.initOwner(tabelaGraficos.getScene().getWindow());
        d.setTitle("Renomear gráfico"); d.setHeaderText("Informe o novo nome do gráfico.");
        d.showAndWait().ifPresent(nome -> { try { controller.renomearGrafico(grafico,nome); tabelaGraficos.refresh(); }
            catch (IllegalArgumentException e) { erro(e.getMessage()); } catch (SQLException e) { erro("Não foi possível renomear o gráfico."); } });
    }

    private void excluirGrafico() {
        Grafico grafico = tabelaGraficos.getSelectionModel().getSelectedItem();
        if (!confirmar("Excluir o gráfico \""+grafico.getNome()+"\" e suas associações de curvas?")) return;
        try { controller.excluirGrafico(grafico); carregarGraficos(); }
        catch (SQLException e) { erro("Não foi possível excluir o gráfico."); }
    }

    private void plotarGrafico() {
        Grafico grafico = tabelaGraficos.getSelectionModel().getSelectedItem();
        if (grafico != null) new TelaPlotagemGrafico(tabelaGraficos.getScene().getWindow(), grafico).exibir();
    }

    private void editarCurva(CurvaDoGrafico existente) {
        Grafico grafico = tabelaGraficos.getSelectionModel().getSelectedItem();
        try {
            List<Coluna> colunas = controller.listarColunas(idExperimento);
            Dialog<ButtonType> d = new Dialog<>(); d.initOwner(tabelaGraficos.getScene().getWindow());
            d.setTitle(existente == null ? "Adicionar curva" : "Alterar curva");
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            TextField nome = new TextField(existente == null ? "" : existente.curva().getNome());
            ComboBox<Coluna> x = combo(colunas); ComboBox<Coluna> y = combo(colunas);
            if (existente == null) { x.getSelectionModel().select(0); y.getSelectionModel().select(1); }
            else { selecionar(x,existente.curva().getIdColunaX()); selecionar(y,existente.curva().getIdColunaY()); }
            GridPane grade = new GridPane(); grade.setHgap(10); grade.setVgap(10); grade.setPadding(new Insets(8));
            grade.addRow(0,new Label("Nome:"),nome); grade.addRow(1,new Label("Eixo X:"),x); grade.addRow(2,new Label("Eixo Y:"),y);
            d.getDialogPane().setContent(grade);
            Button ok=(Button)d.getDialogPane().lookupButton(ButtonType.OK);
            ok.addEventFilter(javafx.event.ActionEvent.ACTION,e->{try{
                if(existente==null)controller.adicionarCurva(grafico,nome.getText(),x.getValue(),y.getValue());
                else controller.alterarCurva(grafico,existente.curva(),nome.getText(),x.getValue(),y.getValue());
            }catch(IllegalArgumentException ex){e.consume();erro(ex.getMessage());}catch(SQLException ex){e.consume();erro("Não foi possível salvar a curva.");}});
            if(d.showAndWait().filter(ButtonType.OK::equals).isPresent())carregarCurvas(grafico);
        } catch (SQLException e) { erro("Não foi possível carregar as colunas do experimento."); }
    }

    private void removerCurva() {
        Grafico grafico=tabelaGraficos.getSelectionModel().getSelectedItem(); CurvaDoGrafico curva=tabelaCurvas.getSelectionModel().getSelectedItem();
        if(!confirmar("Remover a curva \""+curva.curva().getNome()+"\" deste gráfico?"))return;
        try{controller.removerCurva(grafico,curva);carregarCurvas(grafico);}
        catch(IllegalArgumentException e){erro(e.getMessage());}catch(SQLException e){erro("Não foi possível remover a curva.");}
    }

    private ComboBox<Coluna> combo(List<Coluna> colunas){ComboBox<Coluna> c=new ComboBox<>(FXCollections.observableArrayList(colunas));c.setConverter(new StringConverter<>(){public String toString(Coluna v){return descrever(v);}public Coluna fromString(String s){return null;}});c.setMaxWidth(Double.MAX_VALUE);return c;}
    private void selecionar(ComboBox<Coluna> c,Long id){c.getItems().stream().filter(v->v.getId().equals(id)).findFirst().ifPresent(c.getSelectionModel()::select);}
    private String descrever(Coluna c){return c==null?"Coluna não encontrada":rotulo(c.getRotulo())+" — "+c.getNomeColuna();}
    private String rotulo(Short numero){if(numero==null)return"?";int n=numero;StringBuilder r=new StringBuilder();while(n>0){n--;r.insert(0,(char)('A'+n%26));n/=26;}return r.toString();}
    private boolean confirmar(String texto){Alert a=new Alert(Alert.AlertType.CONFIRMATION);a.initOwner(tabelaGraficos.getScene().getWindow());a.setTitle("GACS");a.setHeaderText(texto);a.setContentText("Esta ação não poderá ser desfeita.");return a.showAndWait().filter(ButtonType.OK::equals).isPresent();}
    private void erro(String texto){Alert a=new Alert(Alert.AlertType.ERROR);a.initOwner(janelaPai);a.setTitle("GACS");a.setHeaderText("Não foi possível concluir a operação.");a.setContentText(texto);a.showAndWait();}
}
