package com.contratech.cadastrocontrato.view;

import com.contratech.cadastrocontrato.dao.UsuarioDAO;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;
import com.contratech.cadastrocontrato.util.SenhaUtil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

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
        Label lblTitulo = new Label("CadastroContrato");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        Label lblSubtitulo = new Label("Sistema de Gestão de Contratos");
        lblSubtitulo.setStyle("-fx-text-fill: #7f8c8d;");

        // === CAMPOS ===
        txtEmail = new TextField();
        txtEmail.setPromptText("E-mail");
        txtEmail.setPrefHeight(40);
        txtEmail.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #ddd;"
        );

        txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha");
        txtSenha.setPrefHeight(40);
        txtSenha.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #ddd;"
        );

        // === BOTÃO ===
        btnEntrar = new Button("Entrar");
        btnEntrar.setPrefHeight(45);
        btnEntrar.setMaxWidth(Double.MAX_VALUE);

        String estiloBotao =
                "-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14;" +
                "-fx-cursor: hand;";

        String hoverBotao =
                "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14;" +
                "-fx-scale-x: 1.03;" +
                "-fx-scale-y: 1.03;" +
                "-fx-cursor: hand;";

        btnEntrar.setStyle(estiloBotao);

        btnEntrar.setOnMouseEntered(e -> btnEntrar.setStyle(hoverBotao));
        btnEntrar.setOnMouseExited(e -> btnEntrar.setStyle(estiloBotao));

        // === AÇÃO ===
        btnEntrar.setOnAction(e -> realizarLogin());
        txtSenha.setOnAction(e -> realizarLogin());

        // === FORMULÁRIO (CARD) ===
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
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20,0,0,5);"
        );

        // === FUNDO COM GRADIENTE ===
        StackPane root = new StackPane(formulario);
        root.setAlignment(Pos.CENTER);

        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #eef2f7, #dfe6ee);"
        );

        root.setPadding(new Insets(40));

        // === SCENE ===
        Scene scene = new Scene(root, 500, 420);

        stage.setTitle("Login - CadastroContrato");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        txtEmail.requestFocus();
    }

    // ✅ LÓGICA ORIGINAL MANTIDA
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
            AlertaUtil.erro("Erro", "Erro ao conectar com o banco:\n" + e.getMessage());
        } finally {
            btnEntrar.setDisable(false);
        }
    }
}