package com.contratech.contratos.util.ui;

import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.view.TelaAjuda;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class AjudaUtil {

    private static final String ESTILO_BOTAO_AJUDA =
            "-fx-background-color: rgba(255,255,255,0.2);"
            + "-fx-text-fill: white;"
            + "-fx-background-radius: 20;"
            + "-fx-padding: 6 14;"
            + "-fx-font-weight: bold;";

    private AjudaUtil() {
    }

    public static Button criarBotaoAjuda(Stage stage, Usuario usuario, String topico, Runnable acaoVoltar) {
        Button btnAjuda = new Button("Ajuda");
        btnAjuda.setStyle(ESTILO_BOTAO_AJUDA);
        btnAjuda.setOnAction(e -> abrirAjuda(stage, usuario, topico, acaoVoltar));
        return btnAjuda;
    }

    public static void registrarAtalhoF1(Scene scene, Stage stage, Usuario usuario, String topico, Runnable acaoVoltar) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F1) {
                abrirAjuda(stage, usuario, topico, acaoVoltar);
            }
        });
    }

    public static void abrirAjuda(Stage stage, Usuario usuario, String topico, Runnable acaoVoltar) {
        new TelaAjuda(stage, usuario, topico, acaoVoltar).exibir();
    }
}
