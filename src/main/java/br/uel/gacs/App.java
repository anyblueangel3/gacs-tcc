package br.uel.gacs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palcoPrincipal) {
        Label mensagem = new Label("GACS - Gerenciador para Análise e Caracterização de Componentes Semicondutores");

        Scene cena = new Scene(mensagem, 800, 400);

        palcoPrincipal.setTitle("GACS");
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}