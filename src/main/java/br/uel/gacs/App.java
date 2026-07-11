package br.uel.gacs;

import br.uel.gacs.application.FluxoInicialAplicacao;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palcoPrincipal) {
        new FluxoInicialAplicacao(palcoPrincipal).iniciar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
