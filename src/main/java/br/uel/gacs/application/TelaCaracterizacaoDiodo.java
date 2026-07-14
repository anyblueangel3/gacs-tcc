package br.uel.gacs.application;

import br.uel.gacs.controller.CaracterizacaoDiodoCtlr;
import br.uel.gacs.controller.CaracterizacaoDiodoCtlr.CurvaDisponivel;
import br.uel.gacs.controller.CaracterizacaoDiodoCtlr.ResultadoDiodo;
import br.uel.gacs.model.Experimento;
import br.uel.gacs.util.FormatadorNumero;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

/** Seleciona curvas, executa os cálculos e apresenta a caracterização de diodo. */
public final class TelaCaracterizacaoDiodo {
    private final Window janelaPai;
    private final Long idExperimento;
    private final CaracterizacaoDiodoCtlr controller = new CaracterizacaoDiodoCtlr();

    public TelaCaracterizacaoDiodo(Window janelaPai, Long idExperimento) {
        this.janelaPai = janelaPai;
        this.idExperimento = idExperimento;
    }

    public void exibir() {
        Experimento experimento;
        List<CurvaDisponivel> curvas;
        try {
            experimento = controller.buscarExperimento(idExperimento);
            curvas = controller.listarCurvas(idExperimento);
        } catch (SQLException e) {
            erro("Não foi possível carregar as curvas do experimento.");
            return;
        }
        if (curvas.isEmpty()) {
            erro("O experimento não possui curvas cadastradas.");
            return;
        }

        Stage janela = new Stage();
        janela.initOwner(janelaPai);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle("Caracterização de Diodo — " + experimento.getNomeExperimento());

        ComboBox<CurvaDisponivel> curvaDireta = comboCurvas(curvas);
        ComboBox<CurvaDisponivel> curvaReversa = comboCurvas(curvas);
        selecionarPorPerfil(curvaDireta, curvas, "direta");
        boolean encontrouReversa = selecionarPorPerfil(curvaReversa, curvas, "reversa");
        CheckBox usarReversa = new CheckBox("Incluir curva reversa");
        usarReversa.setSelected(encontrouReversa);
        curvaReversa.disableProperty().bind(usarReversa.selectedProperty().not());

        TextField temperatura = new TextField("300");
        TextField correnteReferencia = new TextField("1E-3");
        TextField tensaoMinima = new TextField("0.20");
        TextField tensaoMaxima = new TextField("0.85");
        TextField tensaoRetificacao = new TextField("0.80");
        for (TextField campo : List.of(temperatura, correnteReferencia, tensaoMinima,
                tensaoMaxima, tensaoRetificacao)) campo.setPrefColumnCount(12);

        GridPane formulario = new GridPane();
        formulario.setHgap(10); formulario.setVgap(9);
        formulario.add(new Label("Curva direta:"), 0, 0);
        formulario.add(curvaDireta, 1, 0, 3, 1);
        formulario.add(usarReversa, 0, 1);
        formulario.add(curvaReversa, 1, 1, 3, 1);
        formulario.addRow(2, new Label("Temperatura (K):"), temperatura,
                new Label("Corrente de referência (A):"), correnteReferencia);
        formulario.addRow(3, new Label("Tensão mínima do ajuste (V):"), tensaoMinima,
                new Label("Tensão máxima do ajuste (V):"), tensaoMaxima);
        formulario.addRow(4, new Label("Tensão da razão de retificação (V):"), tensaoRetificacao);
        ColumnConstraints rotuloEsquerdo = new ColumnConstraints();
        rotuloEsquerdo.setMinWidth(230); rotuloEsquerdo.setPrefWidth(230);
        ColumnConstraints campoEsquerdo = new ColumnConstraints();
        campoEsquerdo.setMinWidth(180); campoEsquerdo.setHgrow(Priority.ALWAYS);
        ColumnConstraints rotuloDireito = new ColumnConstraints();
        rotuloDireito.setMinWidth(235); rotuloDireito.setPrefWidth(235);
        ColumnConstraints campoDireito = new ColumnConstraints();
        campoDireito.setMinWidth(180); campoDireito.setHgrow(Priority.ALWAYS);
        formulario.getColumnConstraints().addAll(
                rotuloEsquerdo, campoEsquerdo, rotuloDireito, campoDireito);
        GridPane.setHgrow(curvaDireta, Priority.ALWAYS);
        GridPane.setHgrow(curvaReversa, Priority.ALWAYS);
        curvaDireta.setMinWidth(0); curvaReversa.setMinWidth(0);
        curvaDireta.setMaxWidth(Double.MAX_VALUE); curvaReversa.setMaxWidth(Double.MAX_VALUE);

        TextArea relatorio = new TextArea();
        relatorio.setEditable(false);
        relatorio.setWrapText(true);
        relatorio.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        relatorio.setPromptText("O relatório será apresentado aqui após o cálculo.");

        Button calcular = new Button("Calcular caracterização");
        Button copiar = new Button("Copiar relatório");
        Button fechar = new Button("Fechar");
        copiar.setDisable(true);
        calcular.setOnAction(e -> {
            try {
                CurvaDisponivel reversa = usarReversa.isSelected() ? curvaReversa.getValue() : null;
                ResultadoDiodo resultado = controller.caracterizar(curvaDireta.getValue(), reversa,
                        numero(temperatura), numero(correnteReferencia), numero(tensaoMinima),
                        numero(tensaoMaxima), numero(tensaoRetificacao));
                relatorio.setText(controller.gerarRelatorio(experimento, curvaDireta.getValue(), reversa, resultado));
                copiar.setDisable(false);
            } catch (IllegalArgumentException ex) {
                erro(ex.getMessage());
            } catch (SQLException ex) {
                erro("Não foi possível carregar os pontos das curvas selecionadas.");
            }
        });
        copiar.setOnAction(e -> {
            ClipboardContent conteudo = new ClipboardContent();
            conteudo.putString(relatorio.getText());
            Clipboard.getSystemClipboard().setContent(conteudo);
        });
        fechar.setOnAction(e -> janela.close());

        Label titulo = new Label("Relatório calculado a partir das curvas persistidas");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        VBox topo = new VBox(10, titulo, formulario, new HBox(8, calcular, copiar));
        topo.setPadding(new Insets(0, 0, 12, 0));
        BorderPane raiz = new BorderPane(relatorio);
        raiz.setPadding(new Insets(14));
        raiz.setTop(topo);
        HBox rodape = new HBox(fechar); rodape.setPadding(new Insets(10, 0, 0, 0));
        raiz.setBottom(rodape);
        janela.setMinWidth(1050);
        janela.setMinHeight(720);
        janela.setScene(new Scene(raiz, 1100, 780));
        janela.showAndWait();
    }

    private ComboBox<CurvaDisponivel> comboCurvas(List<CurvaDisponivel> curvas) {
        ComboBox<CurvaDisponivel> combo = new ComboBox<>(FXCollections.observableArrayList(curvas));
        combo.setConverter(new StringConverter<>() {
            public String toString(CurvaDisponivel item) {
                if (item == null) return "";
                return item.curva().getNome() + " — " + item.colunaX().getNomeColuna()
                        + " × " + item.colunaY().getNomeColuna() + " — "
                        + item.quantidadePontos() + " pontos (" + item.perfil() + ")";
            }
            public CurvaDisponivel fromString(String texto) { return null; }
        });
        return combo;
    }

    private boolean selecionarPorPerfil(ComboBox<CurvaDisponivel> combo,
                                         List<CurvaDisponivel> curvas, String perfil) {
        CurvaDisponivel encontrada = curvas.stream().filter(c -> c.perfil().contains(perfil)).findFirst()
                .orElse(curvas.getFirst());
        combo.getSelectionModel().select(encontrada);
        return encontrada.perfil().contains(perfil);
    }

    private double numero(TextField campo) {
        if (campo.getText() == null || campo.getText().isBlank())
            throw new IllegalArgumentException("Preencha todos os parâmetros numéricos da análise.");
        return FormatadorNumero.converter(campo.getText());
    }

    private void erro(String texto) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(janelaPai);
        alerta.setTitle("GACS");
        alerta.setHeaderText("Caracterização de diodo não concluída");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}
