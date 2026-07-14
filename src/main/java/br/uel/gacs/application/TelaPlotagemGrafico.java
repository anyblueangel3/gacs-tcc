package br.uel.gacs.application;

import br.uel.gacs.controller.GraficoCtlr;
import br.uel.gacs.controller.GraficoCtlr.SerieDoGrafico;
import br.uel.gacs.model.Grafico;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Pré-visualiza, grava em PNG e imprime um gráfico persistido. */
public final class TelaPlotagemGrafico {
    private static final double LARGURA_PREVIA = 1000;
    private static final double ALTURA_PREVIA = 707;
    private final Window janelaPai;
    private final Grafico grafico;
    private final GraficoCtlr controller = new GraficoCtlr();
    private LineChart<Number, Number> chart;

    public TelaPlotagemGrafico(Window janelaPai, Grafico grafico) {
        this.janelaPai = janelaPai;
        this.grafico = grafico;
    }

    public void exibir() {
        try {
            var series = controller.carregarSeries(grafico);
            if (series.stream().allMatch(s -> s.pontos().isEmpty())) {
                erro("As curvas deste gráfico ainda não possuem pares de dados X e Y para plotagem.");
                return;
            }
            chart = criarGrafico(series);
        } catch (SQLException | IllegalArgumentException e) {
            erro("Não foi possível carregar os dados do gráfico. " + e.getMessage());
            return;
        }

        Stage janela = new Stage();
        janela.initOwner(janelaPai);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle("Plotagem — " + grafico.getNome());

        Button salvar = new Button("Salvar PNG");
        Button imprimir = new Button("Imprimir em A4");
        Button fechar = new Button("Fechar");
        salvar.setOnAction(e -> salvarPng(janela));
        imprimir.setOnAction(e -> imprimir(janela));
        fechar.setOnAction(e -> janela.close());
        HBox rodape = new HBox(10, salvar, imprimir, fechar);
        rodape.setPadding(new Insets(10, 0, 0, 0));

        BorderPane raiz = new BorderPane(chart);
        raiz.setPadding(new Insets(14));
        raiz.setBottom(rodape);
        janela.setScene(new Scene(raiz, 1060, 820));
        janela.showAndWait();
    }

    private LineChart<Number, Number> criarGrafico(java.util.List<SerieDoGrafico> series) {
        String eixoX = nomeComum(series.stream().map(SerieDoGrafico::nomeEixoX).toList(), "Eixo X");
        String eixoY = nomeComum(series.stream().map(SerieDoGrafico::nomeEixoY).toList(), "Eixo Y");
        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel(eixoX);
        y.setLabel(eixoY);
        x.setForceZeroInRange(false);
        y.setForceZeroInRange(false);
        x.setTickLabelFormatter(formatadorEixo());
        y.setTickLabelFormatter(formatadorEixo());

        LineChart<Number, Number> resultado = new LineChart<>(x, y);
        resultado.setTitle(grafico.getNome());
        resultado.setCreateSymbols(true);
        resultado.setAnimated(false);
        resultado.setLegendVisible(true);
        resultado.setLegendSide(javafx.geometry.Side.BOTTOM);
        resultado.setAlternativeRowFillVisible(false);
        resultado.setAlternativeColumnFillVisible(false);
        resultado.setPrefSize(LARGURA_PREVIA, ALTURA_PREVIA);
        resultado.setMinSize(600, 424);
        for (SerieDoGrafico serieOrigem : series) {
            if (serieOrigem.pontos().isEmpty()) continue;
            XYChart.Series<Number, Number> serie = new XYChart.Series<>();
            serie.setName(serieOrigem.nome());
            serieOrigem.pontos().forEach(p -> serie.getData().add(new XYChart.Data<>(p.x(), p.y())));
            resultado.getData().add(serie);
        }
        Platform.runLater(() -> estilizarMarcadores(resultado));
        return resultado;
    }

    /** Apresenta cada medida experimental como um círculo preenchido de 6 px. */
    private void estilizarMarcadores(LineChart<Number, Number> grafico) {
        grafico.applyCss();
        grafico.layout();
        for (XYChart.Series<Number, Number> serie : grafico.getData()) {
            Paint cor = serie.getNode() instanceof Path linha ? linha.getStroke() : null;
            if (cor == null) continue;
            String corCss = cor.toString().replace("0x", "#");
            for (XYChart.Data<Number, Number> ponto : serie.getData()) {
                if (ponto.getNode() instanceof javafx.scene.layout.Region marcador) {
                    marcador.setMinSize(6, 6);
                    marcador.setPrefSize(6, 6);
                    marcador.setMaxSize(6, 6);
                    marcador.setStyle("-fx-background-color: " + corCss
                            + "; -fx-background-radius: 3px; -fx-padding: 0;");
                }
            }
        }
    }

    private javafx.util.StringConverter<Number> formatadorEixo() {
        return new javafx.util.StringConverter<>() {
            public String toString(Number numero) { return String.format(java.util.Locale.ROOT, "%.3E", numero.doubleValue()); }
            public Number fromString(String texto) { return Double.valueOf(texto); }
        };
    }

    private String nomeComum(java.util.List<String> nomes, String padrao) {
        return nomes.stream().filter(n -> n != null && !n.isBlank()).distinct().count() == 1
                ? nomes.stream().filter(n -> n != null && !n.isBlank()).findFirst().orElse(padrao) : padrao;
    }

    private void salvarPng(Window janela) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar plotagem do gráfico");
        seletor.setInitialFileName(nomeArquivo(grafico.getNome()) + ".png");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagem PNG", "*.png"));
        File arquivo = seletor.showSaveDialog(janela);
        if (arquivo == null) return;
        if (!arquivo.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
            arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".png");
        }
        double largura = chart.getWidth();
        double altura = chart.getHeight();
        double escala = Math.min(2480.0 / largura, 1754.0 / altura);
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.WHITE);
        parametros.setTransform(new Scale(escala, escala));
        WritableImage imagem = new WritableImage((int)Math.ceil(largura * escala),
                (int)Math.ceil(altura * escala));
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(chart.snapshot(parametros, imagem), null), "png", arquivo);
            informar("Gráfico salvo", "A imagem PNG foi gravada em:\n" + arquivo.getAbsolutePath());
        } catch (IOException e) {
            erro("Não foi possível gravar a imagem PNG.");
        }
    }

    private void imprimir(Window janela) {
        PrinterJob trabalho = PrinterJob.createPrinterJob();
        if (trabalho == null) { erro("Nenhuma impressora está disponível."); return; }
        if (!trabalho.showPrintDialog(janela)) return;
        Printer impressora = trabalho.getPrinter();
        PageLayout pagina = impressora.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE,
                Printer.MarginType.DEFAULT);

        double escala = Math.min(pagina.getPrintableWidth() / chart.getBoundsInParent().getWidth(),
                pagina.getPrintableHeight() / chart.getBoundsInParent().getHeight());
        Scale transformacao = new Scale(escala, escala);
        chart.getTransforms().add(transformacao);
        boolean sucesso;
        try { sucesso = trabalho.printPage(pagina, chart); }
        finally { chart.getTransforms().remove(transformacao); }
        if (sucesso) { trabalho.endJob(); informar("Impressão", "O gráfico foi enviado para impressão em A4 horizontal."); }
        else { trabalho.cancelJob(); erro("Não foi possível imprimir o gráfico."); }
    }

    private String nomeArquivo(String nome) {
        String seguro = nome == null ? "grafico" : nome.trim().replaceAll("[^\\p{L}\\p{N}._-]+", "_");
        return seguro.isBlank() ? "grafico" : seguro;
    }

    private void informar(String titulo, String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(janelaPai); a.setTitle("GACS"); a.setHeaderText(titulo); a.setContentText(texto); a.showAndWait();
    }

    private void erro(String texto) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(janelaPai); a.setTitle("GACS"); a.setHeaderText("Plotagem não concluída"); a.setContentText(texto); a.showAndWait();
    }
}
