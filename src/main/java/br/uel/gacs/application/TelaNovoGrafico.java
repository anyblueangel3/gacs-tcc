package br.uel.gacs.application;

import br.uel.gacs.controller.GraficoCtlr;
import br.uel.gacs.model.Coluna;
import br.uel.gacs.model.Grafico;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.StringConverter;

/**
 * Diálogo de registro de um gráfico e de sua primeira curva.
 *
 * @author Ronaldo Rodrigues Godoi e Chat GPT
 */
public final class TelaNovoGrafico {
    private final Window janela;
    private final GraficoCtlr controller;
    private final Long idExperimento;

    public TelaNovoGrafico(Window janela, Long idExperimento) {
        this(janela, idExperimento, new GraficoCtlr());
    }

    TelaNovoGrafico(Window janela, Long idExperimento, GraficoCtlr controller) {
        this.janela = janela;
        this.idExperimento = idExperimento;
        this.controller = controller;
    }

    public boolean exibir() {
        if (idExperimento == null) {
            erro("Salve o experimento antes de criar um gráfico.");
            return false;
        }
        final List<Coluna> colunas;
        try { colunas = controller.listarColunas(idExperimento); }
        catch (SQLException e) { erro("Não foi possível carregar as colunas do experimento."); return false; }
        if (colunas.size() < 2) {
            erro("O experimento precisa possuir ao menos duas colunas salvas para formar uma curva.");
            return false;
        }

        Dialog<Grafico> dialogo = new Dialog<>();
        dialogo.initOwner(janela);
        dialogo.setTitle("Novo Gráfico");
        dialogo.setHeaderText("Registre o gráfico e a primeira curva que o compõe.");
        ButtonType salvar = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(salvar, ButtonType.CANCEL);

        TextField nomeGrafico = new TextField(); nomeGrafico.setPromptText("Nome do gráfico");
        TextField nomeCurva = new TextField(); nomeCurva.setPromptText("Nome da curva");
        ComboBox<Coluna> colunaX = new ComboBox<>(); colunaX.getItems().addAll(colunas);
        ComboBox<Coluna> colunaY = new ComboBox<>(); colunaY.getItems().addAll(colunas);
        StringConverter<Coluna> conversor = conversorColuna();
        colunaX.setConverter(conversor); colunaY.setConverter(conversor);
        colunaX.getSelectionModel().select(0); colunaY.getSelectionModel().select(1);
        colunaX.setMaxWidth(Double.MAX_VALUE); colunaY.setMaxWidth(Double.MAX_VALUE);

        GridPane grade = new GridPane(); grade.setHgap(10); grade.setVgap(10); grade.setPadding(new Insets(8));
        grade.addRow(0, new Label("Gráfico:"), nomeGrafico);
        grade.addRow(1, new Label("Curva:"), nomeCurva);
        grade.addRow(2, new Label("Eixo X:"), colunaX);
        grade.addRow(3, new Label("Eixo Y:"), colunaY);
        dialogo.getDialogPane().setContent(grade);

        Button botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(salvar);
        AtomicReference<Grafico> graficoRegistrado = new AtomicReference<>();
        botaoSalvar.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            try {
                Grafico grafico = controller.criarComPrimeiraCurva(idExperimento,
                        nomeGrafico.getText(), nomeCurva.getText(), colunaX.getValue(), colunaY.getValue());
                graficoRegistrado.set(grafico);
            } catch (IllegalArgumentException e) {
                evento.consume(); erro(e.getMessage());
            } catch (SecurityException e) {
                evento.consume(); erro(e.getMessage());
            } catch (SQLException e) {
                evento.consume(); erro("Não foi possível registrar o gráfico e sua curva.");
            }
        });
        dialogo.setResultConverter(tipo -> tipo == salvar ? graficoRegistrado.get() : null);
        java.util.Optional<Grafico> resultado = dialogo.showAndWait();
        resultado.ifPresent(g -> informarSucesso(g.getNome()));
        return resultado.isPresent();
    }

    private StringConverter<Coluna> conversorColuna() {
        return new StringConverter<>() {
            @Override public String toString(Coluna c) {
                if (c == null) return "";
                return rotulo(c.getRotulo()) + " — " + c.getNomeColuna();
            }
            @Override public Coluna fromString(String texto) { return null; }
        };
    }

    private String rotulo(Short numero) {
        if (numero == null) return "?";
        int n = numero; StringBuilder resultado = new StringBuilder();
        while (n > 0) { n--; resultado.insert(0, (char) ('A' + n % 26)); n /= 26; }
        return resultado.toString();
    }

    private void informarSucesso(String nome) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.initOwner(janela); a.setTitle("GACS");
        a.setHeaderText("Gráfico registrado"); a.setContentText("O gráfico \"" + nome + "\" e sua primeira curva foram salvos."); a.showAndWait();
    }

    private void erro(String texto) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.initOwner(janela); a.setTitle("GACS");
        a.setHeaderText("Não foi possível concluir a operação."); a.setContentText(texto); a.showAndWait();
    }
}
