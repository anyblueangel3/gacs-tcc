package br.uel.gacs.application;

import br.uel.gacs.controller.ExperimentoCtlr;
import br.uel.gacs.controller.ExperimentoCtlr.ExperimentoListado;
import br.uel.gacs.controller.GraficoCtlr;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.Usuario;
import br.uel.gacs.util.LeitorCsv;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import javafx.stage.Window;
import javafx.stage.FileChooser;

/** Janela principal e espaço permanente de trabalho do GACS. */
public final class TelaPrincipal {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Window janela;
    private final Runnable acaoSair;
    private final ExperimentoCtlr controller = new ExperimentoCtlr();
    private final GraficoCtlr graficoController = new GraficoCtlr();
    private final BorderPane raiz = new BorderPane();
    private MenuPrincipal menu;
    private PainelExperimento painelAtual;

    public TelaPrincipal(Window janela, Runnable acaoSair) {
        if (janela == null || acaoSair == null) throw new IllegalArgumentException("A janela e a ação de saída devem ser informadas.");
        this.janela=janela;this.acaoSair=acaoSair;
    }

    public Parent criar() {
        Usuario usuario=SessaoUsuario.exigirUsuarioLogado();
        menu=new MenuPrincipal(janela,acaoSair,()->new TelaCadastroUsuarios(janela).exibir(),
                ()->iniciarNovo(null),this::exibirListaExperimentos,this::colarPlanilha,
                this::importarCsv,
                this::digitarDados, this::novoGrafico, this::caracterizarComponente);
        raiz.setTop(menu.criar()); raiz.setBottom(criarBarraEstado(usuario));
        raiz.setStyle("-fx-background-color: #f7f9fb;"); exibirListaExperimentos(); return raiz;
    }

    private void exibirListaExperimentos() {
        painelAtual = null;
        menu.definirExperimentoAberto(false);
        Usuario usuario=SessaoUsuario.exigirUsuarioLogado();
        Label titulo=new Label("Experimentos disponíveis");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #244766;");
        TableView<ExperimentoListado> tabela=new TableView<>();
        TableColumn<ExperimentoListado,String> nome=coluna("Experimento",i->i.experimento().getNomeExperimento());
        TableColumn<ExperimentoListado,String> data=coluna("Data",i->i.experimento().getDataExperimento().format(DATA));
        TableColumn<ExperimentoListado,String> proprietario=coluna("Proprietário",ExperimentoListado::proprietario);
        nome.setPrefWidth(470);data.setPrefWidth(170);proprietario.setPrefWidth(260);
        tabela.getColumns().addAll(nome,data,proprietario);
        tabela.setPlaceholder(new Label("Nenhum experimento encontrado."));
        Button abrir=new Button("Abrir Experimento");abrir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        Button excluir=new Button("Excluir Experimento");
        excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        abrir.setOnAction(e->abrir(tabela.getSelectionModel().getSelectedItem()));
        excluir.setOnAction(e->excluirExperimento(tabela.getSelectionModel().getSelectedItem()));
        tabela.setRowFactory(tv->{TableRow<ExperimentoListado> linha=new TableRow<>();linha.setOnMouseClicked(e->{if(e.getClickCount()==2&&!linha.isEmpty())abrir(linha.getItem());});return linha;});
        try { tabela.setItems(FXCollections.observableArrayList(controller.listarVisiveis(usuario,Autorizador.usuarioAtualEhAdministrador()))); }
        catch(SQLException e){erro("Não foi possível carregar a lista de experimentos.");}
        VBox centro=new VBox(12,titulo,tabela,new HBox(8,abrir,excluir));centro.setPadding(new Insets(18));VBox.setVgrow(tabela,Priority.ALWAYS);raiz.setCenter(centro);
    }

    private void excluirExperimento(ExperimentoListado item) {
        if (item == null) return;
        Usuario usuario = SessaoUsuario.exigirUsuarioLogado();
        boolean podeExcluir = Autorizador.usuarioAtualEhAdministrador()
                || usuario.getId().equals(item.experimento().getIdUsuario());
        if (!podeExcluir) { erro("Somente o proprietário do experimento ou um administrador pode excluí-lo."); return; }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.initOwner(janela);
        confirmacao.setTitle("Excluir experimento");
        confirmacao.setHeaderText("Excluir definitivamente o experimento \"" + item.experimento().getNomeExperimento() + "\"?");
        confirmacao.setContentText("Também serão excluídos suas colunas, dados, curvas e gráficos. Esta ação não poderá ser desfeita.");
        if (confirmacao.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        try {
            controller.excluirExperimento(item.experimento(), usuario, Autorizador.usuarioAtualEhAdministrador());
            exibirListaExperimentos();
        } catch (SecurityException e) { erro(e.getMessage());
        } catch (SQLException e) { erro("Não foi possível excluir o experimento."); }
    }

    private void abrir(ExperimentoListado item){
        if(item==null)return;
        try { exibirExperimento(item.experimento(),item.proprietario(),controller.carregarPlanilha(item.experimento().getId())); }
        catch(SQLException e){erro("Não foi possível carregar os dados do experimento.");}
    }
    private void iniciarNovo(PlanilhaExperimento planilha){Usuario u=SessaoUsuario.exigirUsuarioLogado();exibirExperimento(controller.criarNovo(u.getId()),u.getNome(),planilha);}
    private void exibirExperimento(Experimento e,String proprietario,PlanilhaExperimento planilha){
        menu.definirExperimentoAberto(true);
        painelAtual=new PainelExperimento(janela,controller,e,proprietario,planilha,this::exibirListaExperimentos);
        raiz.setCenter(painelAtual.criar());
        atualizarDisponibilidadeCaracterizacao();
    }

    private void digitarDados() {
        if (painelAtual != null) painelAtual.adicionarColunaParaDigitacao();
        else iniciarNovo(criarPlanilhaDigitacao());
    }

    private void novoGrafico() {
        if (painelAtual != null) {
            new TelaGraficos(janela, painelAtual.getIdExperimento()).exibir();
            atualizarDisponibilidadeCaracterizacao();
        }
    }

    private void caracterizarComponente() {
        if (painelAtual != null && painelAtual.getIdExperimento() != null)
            new TelaCaracterizacaoComponente(janela, painelAtual.getIdExperimento()).exibir();
    }

    private void atualizarDisponibilidadeCaracterizacao() {
        Long idExperimento = painelAtual == null ? null : painelAtual.getIdExperimento();
        try { menu.definirCaracterizacaoDisponivel(graficoController.possuiCurvas(idExperimento)); }
        catch (SQLException e) {
            menu.definirCaracterizacaoDisponivel(false);
            erro("Não foi possível verificar as curvas disponíveis para caracterização.");
        }
    }

    private void colarPlanilha() {
        String texto=Clipboard.getSystemClipboard().getString();
        if(texto==null||texto.isBlank()){erro("A área de transferência não contém dados de texto.");return;}
        try{receberPlanilha(PlanilhaExperimento.deCelulas(separarCelulas(texto)));}catch(IllegalArgumentException e){erro(e.getMessage());}
    }

    private void importarCsv() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Importar dados de arquivo CSV");
        seletor.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*"));
        java.io.File arquivo = seletor.showOpenDialog(janela);
        if (arquivo == null) return;
        try { receberPlanilha(PlanilhaExperimento.deCelulas(LeitorCsv.ler(arquivo.toPath()))); }
        catch (IOException e) { erro("Não foi possível ler o arquivo selecionado."); }
        catch (IllegalArgumentException e) { erro(e.getMessage()); }
    }

    private void receberPlanilha(PlanilhaExperimento planilha) {
        if(painelAtual==null)iniciarNovo(planilha);
        else painelAtual.acrescentarPlanilha(planilha);
    }

    private List<List<String>> separarCelulas(String texto) {
        String[] linhas=texto.strip().split("\\R",-1);
        List<List<String>> dados=new ArrayList<>();
        for(String linha:linhas){dados.add(List.copyOf(Arrays.asList(linha.split("\\t",-1))));}
        return List.copyOf(dados);
    }
    private PlanilhaExperimento criarPlanilhaDigitacao(){PlanilhaExperimento p=new PlanilhaExperimento();p.adicionarColuna("X");p.adicionarColuna("Y");for(int i=0;i<20;i++)p.adicionarMedidaVazia();return p;}
    private TableColumn<ExperimentoListado,String> coluna(String titulo,java.util.function.Function<ExperimentoListado,String> valor){TableColumn<ExperimentoListado,String> c=new TableColumn<>(titulo);c.setCellValueFactory(i->new ReadOnlyStringWrapper(valor.apply(i.getValue())));return c;}
    private Label criarBarraEstado(Usuario u){Label l=new Label("Usuário conectado: "+u.getNome());l.setMaxWidth(Double.MAX_VALUE);l.setPadding(new Insets(8,14,8,14));l.setStyle("-fx-background-color: #244766; -fx-text-fill: white;");return l;}
    private void erro(String texto){Alert a=new Alert(Alert.AlertType.ERROR);a.initOwner(janela);a.setTitle("GACS");a.setHeaderText("Operação não concluída");a.setContentText(texto);a.showAndWait();}
}
