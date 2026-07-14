package br.uel.gacs.application;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

/** Porta de entrada para as rotinas de caracterização de componentes. */
public final class TelaCaracterizacaoComponente {
    private final Window janelaPai;
    private final Long idExperimento;

    public TelaCaracterizacaoComponente(Window janelaPai, Long idExperimento) {
        if (janelaPai == null || idExperimento == null) {
            throw new IllegalArgumentException("A janela e o experimento devem ser informados.");
        }
        this.janelaPai = janelaPai;
        this.idExperimento = idExperimento;
    }

    public void exibir() {
        ButtonType diodo = new ButtonType("Diodo", ButtonBar.ButtonData.OTHER);
        ButtonType fet = new ButtonType("FET", ButtonBar.ButtonData.OTHER);
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(janelaPai);
        dialogo.setTitle("Caracterização de Componente");
        dialogo.setHeaderText("Selecione o tipo de componente a caracterizar.");
        dialogo.getDialogPane().setContentText(
                "A caracterização utilizará as curvas cadastradas no experimento.");
        dialogo.getDialogPane().getButtonTypes().addAll(diodo, fet, ButtonType.CANCEL);
        dialogo.showAndWait().ifPresent(escolha -> {
            if (escolha == diodo) new TelaCaracterizacaoDiodo(janelaPai, idExperimento).exibir();
            else if (escolha == fet) informar("Caracterização de FET");
        });
    }

    private void informar(String tipo) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(janelaPai);
        alerta.setTitle("GACS");
        alerta.setHeaderText(tipo);
        alerta.setContentText("A caracterização de FET será implementada em um próximo incremento.");
        alerta.showAndWait();
    }
}
