package com.contratech.cadastrocontrato.view;

import com.contratech.cadastrocontrato.dao.UsuarioDAO;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;
import com.contratech.cadastrocontrato.util.SenhaUtil;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TelaLogin {

    private final Stage stage;

    private TextField txtEmail;
    private PasswordField txtSenha;
    private Button btnEntrar;

    public TelaLogin(Stage stage) {
        this.stage = stage;
    }

    public void exibir() {

        // === TÍTULO ===
        Label lblTitulo = new Label("Contratech");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblSubtitulo = new Label("Sistema de Gestão de Contratos");
        lblSubtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        // === CAMPOS ===
        txtEmail = new TextField();
        txtEmail.setPromptText("E-mail");
        txtEmail.setPrefHeight(40);

        txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha");
        txtSenha.setPrefHeight(40);

        // === BOTÃO ===
        btnEntrar = new Button("Entrar");
        btnEntrar.setPrefHeight(45);
        btnEntrar.setMaxWidth(Double.MAX_VALUE);

        String estiloBotao =
                "-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-font-weight: bold;";

        btnEntrar.setStyle(estiloBotao);

        btnEntrar.setOnAction(e -> realizarLogin());
        txtSenha.setOnAction(e -> realizarLogin());

        // === FORMULÁRIO ===
        VBox formulario = new VBox(15,
                lblTitulo,
                lblSubtitulo,
                new Separator(),
                txtEmail,
                txtSenha,
                btnEntrar
        );

        formulario.setAlignment(Pos.CENTER);
        formulario.setPadding(new Insets(40));
        formulario.setMaxWidth(420);

        formulario.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 25,0,0,10);"
        );

        // === ROOT ===
        StackPane root = new StackPane();
        root.setPadding(new Insets(40));

        // ===== FUNDO BASE =====
        root.setStyle("-fx-background-color: #2c3e50;");

        // ===== FORMAS (EFEITO MOVIMENTO) =====

        Circle c1 = new Circle(350);
        c1.setFill(new RadialGradient(
                0, 0,
                0.3, 0.3,
                1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4ca1af")),
                new Stop(1, Color.TRANSPARENT)
        ));
        c1.setOpacity(0.6);

        Circle c2 = new Circle(280);
        c2.setFill(new RadialGradient(
                0, 0,
                0.7, 0.7,
                1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c3e50")),
                new Stop(1, Color.TRANSPARENT)
        ));
        c2.setOpacity(0.5);

        Circle c3 = new Circle(220);
        c3.setFill(new RadialGradient(
                0, 0,
                0.5, 0.5,
                1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4ca1af")),
                new Stop(1, Color.TRANSPARENT)
        ));
        c3.setOpacity(0.4);

        // Adiciona ordem correta (fundo primeiro)
        root.getChildren().addAll(c1, c2, c3, formulario);

        // ===== ANIMAÇÕES =====

        TranslateTransition t1 = new TranslateTransition(Duration.seconds(12), c1);
        t1.setFromX(-300);
        t1.setToX(300);
        t1.setAutoReverse(true);
        t1.setCycleCount(TranslateTransition.INDEFINITE);
        t1.play();

        TranslateTransition t2 = new TranslateTransition(Duration.seconds(15), c2);
        t2.setFromY(-200);
        t2.setToY(200);
        t2.setAutoReverse(true);
        t2.setCycleCount(TranslateTransition.INDEFINITE);
        t2.play();

        TranslateTransition t3 = new TranslateTransition(Duration.seconds(18), c3);
        t3.setFromX(200);
        t3.setToX(-200);
        t3.setAutoReverse(true);
        t3.setCycleCount(TranslateTransition.INDEFINITE);
        t3.play();

        // === SCENE ===
        Scene scene = new Scene(root, 500, 420);

        stage.setTitle("Login - CadastroContrato");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        txtEmail.requestFocus();
    }

    private void realizarLogin() {

        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            AlertaUtil.aviso("Campos obrigatórios", "Preencha o e-mail e a senha.");
            return;
        }

        btnEntrar.setDisable(true);

        try {
            String senhaHash = SenhaUtil.hashSHA256(senha);

            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.autenticar(email, senhaHash);

            if (usuario != null) {
                new TelaPrincipal(stage, usuario).exibir();
            } else {
                AlertaUtil.erro("Falha no login", "E-mail ou senha inválidos.");
                txtSenha.clear();
                txtSenha.requestFocus();
            }

        } catch (Exception e) {
            AlertaUtil.erro("Erro", "Erro ao conectar:\n" + e.getMessage());
        } finally {
            btnEntrar.setDisable(false);
        }
    }
}