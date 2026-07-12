package br.uel.gacs.application;

import br.uel.gacs.controller.ExperimentoCtlr;
import br.uel.gacs.controller.ExperimentoCtlr.ExperimentoListado;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.model.Usuario;
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

/** Janela principal e espaço permanente de trabalho do GACS. */
public final class TelaPrincipal {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Window janela;
    private final Runnable acaoSair;
    private final ExperimentoCtlr controller = new ExperimentoCtlr();
    private final BorderPane raiz = new BorderPane();
    private MenuPrincipal menu;

    public TelaPrincipal(Window janela, Runnable acaoSair) {
        if (janela == null || acaoSair == null) throw new IllegalArgumentException("A janela e a ação de saída devem ser informadas.");
        this.janela=janela;this.acaoSair=acaoSair;
    }

    public Parent criar() {
        Usuario usuario=SessaoUsuario.exigirUsuarioLogado();
        menu=new MenuPrincipal(janela,acaoSair,()->new TelaCadastroUsuarios(janela).exibir(),
                ()->iniciarNovo(null),this::exibirListaExperimentos,this::colarPlanilha,
                ()->iniciarNovo(null));
        raiz.setTop(menu.criar()); raiz.setBottom(criarBarraEstado(usuario));
        raiz.setStyle("-fx-background-color: #f7f9fb;"); exibirListaExperimentos(); return raiz;
    }

    private void exibirListaExperimentos() {
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
        abrir.setOnAction(e->abrir(tabela.getSelectionModel().getSelectedItem()));
        tabela.setRowFactory(tv->{TableRow<ExperimentoListado> linha=new TableRow<>();linha.setOnMouseClicked(e->{if(e.getClickCount()==2&&!linha.isEmpty())abrir(linha.getItem());});return linha;});
        try { tabela.setItems(FXCollections.observableArrayList(controller.listarVisiveis(usuario,Autorizador.usuarioAtualEhAdministrador()))); }
        catch(SQLException e){erro("Não foi possível carregar a lista de experimentos.");}
        VBox centro=new VBox(12,titulo,tabela,abrir);centro.setPadding(new Insets(18));VBox.setVgrow(tabela,Priority.ALWAYS);raiz.setCenter(centro);
    }

    private void abrir(ExperimentoListado item){if(item!=null)exibirExperimento(item.experimento(),item.proprietario(),null);}
    private void iniciarNovo(List<List<String>> dados){Usuario u=SessaoUsuario.exigirUsuarioLogado();exibirExperimento(controller.criarNovo(u.getId()),u.getNome(),dados);}
    private void exibirExperimento(Experimento e,String proprietario,List<List<String>> dados){menu.definirExperimentoAberto(true);raiz.setCenter(new PainelExperimento(janela,controller,e,proprietario,dados,this::exibirListaExperimentos).criar());}

    private void colarPlanilha() {
        String texto=Clipboard.getSystemClipboard().getString();
        if(texto==null||texto.isBlank()){erro("A área de transferência não contém dados de texto.");return;}
        try{iniciarNovo(validarPlanilha(texto));}catch(IllegalArgumentException e){erro(e.getMessage());}
    }

    private List<List<String>> validarPlanilha(String texto) {
        String[] linhas=texto.strip().split("\\R",-1);
        if(linhas.length>10_001)throw new IllegalArgumentException("A planilha ultrapassa o limite de 10.000 medidas.");
        List<List<String>> dados=new ArrayList<>();int quantidade=-1;
        for(String linha:linhas){List<String>celulas=Arrays.asList(linha.split("\\t",-1));if(quantidade<0)quantidade=celulas.size();if(quantidade>50)throw new IllegalArgumentException("A planilha ultrapassa o limite de 50 colunas.");if(celulas.size()!=quantidade)throw new IllegalArgumentException("As linhas copiadas não possuem a mesma quantidade de colunas.");dados.add(List.copyOf(celulas));}
        int inicio=possuiCabecalho(dados.getFirst())?1:0;
        for(int l=inicio;l<dados.size();l++)for(String valor:dados.get(l))if(!valor.isBlank())try{Double.parseDouble(valor.trim().replace(',','.'));}catch(NumberFormatException e){throw new IllegalArgumentException("Foi encontrado um valor não numérico na linha "+(l+1)+".");}
        return List.copyOf(dados);
    }
    private boolean possuiCabecalho(List<String> linha){for(String v:linha)if(!v.isBlank())try{Double.parseDouble(v.trim().replace(',','.'));}catch(NumberFormatException e){return true;}return false;}
    private TableColumn<ExperimentoListado,String> coluna(String titulo,java.util.function.Function<ExperimentoListado,String> valor){TableColumn<ExperimentoListado,String> c=new TableColumn<>(titulo);c.setCellValueFactory(i->new ReadOnlyStringWrapper(valor.apply(i.getValue())));return c;}
    private Label criarBarraEstado(Usuario u){Label l=new Label("Usuário conectado: "+u.getNome());l.setMaxWidth(Double.MAX_VALUE);l.setPadding(new Insets(8,14,8,14));l.setStyle("-fx-background-color: #244766; -fx-text-fill: white;");return l;}
    private void erro(String texto){Alert a=new Alert(Alert.AlertType.ERROR);a.initOwner(janela);a.setTitle("GACS");a.setHeaderText("Operação não concluída");a.setContentText(texto);a.showAndWait();}
}
