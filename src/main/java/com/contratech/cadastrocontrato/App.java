package com.contratech.cadastrocontrato;



import com.contratech.cadastrocontrato.view.TelaLogin;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação JavaFX.
 * 
 * Application.launch() inicializa o runtime do JavaFX
 * e chama o método start() automaticamente.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        TelaLogin telaLogin = new TelaLogin(stage);
        telaLogin.exibir();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
