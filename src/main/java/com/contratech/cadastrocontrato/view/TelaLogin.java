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

/**
 * Tela de Login do sistema.
 * 
 * Responsável por autenticar o usuário e direcionar para a tela principal.
 * O usuário autenticado é armazenado e passado adiante para controle de permissões.
 */
public class TelaLogin {

    private final Stage stage;

    // Componentes do formulário
    private TextField txtEmail;
    private PasswordField txtSenha;
    private Button btnEntrar;

    public TelaLogin(Stage stage) {
        this.stage = stage;
    }

    public void exibir() {
        // === Título ===
        Label lblTitulo = new Label("CadastroContrato");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label lblSubtitulo = new Label("Sistema de Gestão de Contratos");
        lblSubtitulo.setFont(Font.font("Arial", 13));
        lblSubtitulo.setStyle("-fx-text-fill: #666;");

        // === Campos do formulário ===
        Label lblEmail = new Label("E-mail:");
        txtEmail = new TextField();
        txtEmail.setPromptText("seu@email.com");
        txtEmail.setPrefWidth(280);

        Label lblSenha = new Label("Senha:");
        txtSenha = new PasswordField();
        txtSenha.setPromptText("Digite sua senha");
        txtSenha.setPrefWidth(280);

        // === Botão Entrar ===
        btnEntrar = new Button("Entrar");
        btnEntrar.setPrefWidth(280);
        btnEntrar.setPrefHeight(35);
        btnEntrar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; "
                         + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

        // Ação do botão
        btnEntrar.setOnAction(e -> realizarLogin());

        // Enter no campo senha também faz login
        txtSenha.setOnAction(e -> realizarLogin());

        // === Layout ===
        VBox formulario = new VBox(10);
        formulario.setAlignment(Pos.CENTER);
        formulario.setPadding(new Insets(40, 50, 40, 50));
        formulario.setMaxWidth(380);
        formulario.setStyle("-fx-background-color: white; "
                          + "-fx-background-radius: 8; "
                          + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);");

        formulario.getChildren().addAll(
            lblTitulo,
            lblSubtitulo,
            new Separator(),
            lblEmail, txtEmail,
            lblSenha, txtSenha,
            btnEntrar
        );

        // Fundo cinza atrás do card
        StackPane root = new StackPane(formulario);
        root.setStyle("-fx-background-color: #ECEFF1;");
        root.setPadding(new Insets(50));

        // === Cena e Stage ===
        Scene scene = new Scene(root, 500, 420);
        stage.setTitle("Login - CadastroContrato");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        // Foco no campo email ao abrir
        txtEmail.requestFocus();
    }

    /**
     * Executa a lógica de autenticação.
     * 
     * Fluxo:
     * 1. Valida se os campos estão preenchidos
     * 2. Gera o hash da senha digitada
     * 3. Consulta o banco via DAO
     * 4. Se válido, abre a tela principal passando o usuário logado
     * 5. Se inválido, exibe mensagem de erro genérica (segurança)
     */
    private void realizarLogin() {
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText().trim();

        // Validação básica
        if (email.isEmpty() || senha.isEmpty()) {
            AlertaUtil.aviso("Campos obrigatórios", "Preencha o e-mail e a senha.");
            return;
        }

        // Desabilita botão durante a consulta (evita duplo clique)
        btnEntrar.setDisable(true);

        try {
            String senhaHash = SenhaUtil.hashSHA256(senha);
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.autenticar(email, senhaHash);

            if (usuario != null) {
                // Login bem-sucedido — abre a tela principal
                TelaPrincipal telaPrincipal = new TelaPrincipal(stage, usuario);
                telaPrincipal.exibir();
            } else {
                // Mensagem genérica por segurança — não revela se o email existe
                AlertaUtil.erro("Falha no login", "E-mail ou senha inválidos.");
                txtSenha.clear();
                txtSenha.requestFocus();
            }

        } catch (Exception e) {
            AlertaUtil.erro("Erro", "Erro ao conectar com o banco de dados:\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            btnEntrar.setDisable(false);
        }
    }
}

