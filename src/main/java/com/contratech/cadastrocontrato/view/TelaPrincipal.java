package com.contratech.cadastrocontrato.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.contratech.cadastrocontrato.dao.ContratoDAO;
import com.contratech.cadastrocontrato.model.Contrato;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;

import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class TelaPrincipal {

    private final Stage stage;
    private final Usuario usuarioLogado;

    public TelaPrincipal(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {

        // === HEADER ===
        Label lblBemVindo = new Label("Bem-vindo, " + usuarioLogado.getNome());
        lblBemVindo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblBemVindo.setStyle("-fx-text-fill: white;");

        Label lblPerfil = new Label(usuarioLogado.getTipoUsuario().toString());
        lblPerfil.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-padding: 5 10;" +
                "-fx-background-radius: 20;" +
                "-fx-text-fill: white;"
        );

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle(
                "-fx-background-color: #e74c3c;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 6 16;" +
                "-fx-font-weight: bold;"
        );

        btnLogout.setOnAction(e -> {
            if (AlertaUtil.confirmar("Logout", "Deseja sair?")) {
                new TelaLogin(stage).exibir();
            }
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox header = new HBox(15, lblBemVindo, lblPerfil, espaco, btnLogout);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
        );

        // === BOTÕES ===
        Button btnUsuarios = criarCard("👤 Usuários", "#3498db");
        Button btnParceiros = criarCard("🤝 Parceiros", "#2ecc71");
        Button btnContratos = criarCard("📄 Contratos", "#f39c12");

        btnUsuarios.setOnAction(e -> {
            if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.ADMIN) {
                new TelaUsuario(stage, usuarioLogado).exibir();
            } else {
                AlertaUtil.erro("Acesso negado", "Somente ADMIN");
            }
        });

        btnParceiros.setOnAction(e -> new TelaParceiro(stage, usuarioLogado).exibir());
        btnContratos.setOnAction(e -> new TelaContrato(stage, usuarioLogado).exibir());

        if (usuarioLogado.getTipoUsuario() != Usuario.TipoUsuario.ADMIN) {
            btnUsuarios.setOpacity(0.5);
            btnUsuarios.setTooltip(new Tooltip("Somente ADMIN"));
        }

        HBox menu = new HBox(30, btnUsuarios, btnParceiros, btnContratos);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(50));

        // === FOOTER ===
        Label rodapeTxt = new Label("Sistema CadastroContrato • v1.0");
        rodapeTxt.setStyle("-fx-text-fill: #95a5a6;");

        HBox footer = new HBox(rodapeTxt);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));

        // === ROOT ===
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(menu);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: #eef2f7;");

        Scene scene = new Scene(root, 900, 500);
        stage.setScene(scene);
        stage.setTitle("CadastroContrato");

        stage.show();

        Platform.runLater(() -> {
            stage.setMaximized(true);
            var b = Screen.getPrimary().getVisualBounds();
            stage.setWidth(b.getWidth());
            stage.setHeight(b.getHeight());
        });

        verificarContratosVencidos();
    }

    // === BOTÕES ESTILO CARD ===
    private Button criarCard(String texto, String cor) {

        Button btn = new Button(texto);

        btn.setPrefSize(220, 130);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        String estiloBase =
                "-fx-background-color: white;" +
                "-fx-text-fill: #2c3e50;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-color: #ddd;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8,0,0,2);";

        String hover =
                "-fx-background-color: " + cor + ";" +
                "-fx-text-fill: white;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15,0,0,4);";

        btn.setStyle(estiloBase);

        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));

        return btn;
    }

    // ✅ ALERTA COMPLETO (SEU ORIGINAL MELHORADO)
    private void verificarContratosVencidos() {

        ContratoDAO contratoDAO = new ContratoDAO();
        List<Contrato> contratos = contratoDAO.listarVencidos();

        if (contratos.isEmpty()) return;

        LocalDate hoje = LocalDate.now();

        List<Contrato> vencidos = new ArrayList<>();
        List<Contrato> aVencer = new ArrayList<>();

        for (Contrato c : contratos) {
            if (c.getDataFim() != null && c.getDataFim().isBefore(hoje)) {
                vencidos.add(c);
            } else {
                aVencer.add(c);
            }
        }

        StringBuilder sb = new StringBuilder();

        if (!vencidos.isEmpty()) {
            sb.append("⚠ CONTRATOS VENCIDOS (").append(vencidos.size()).append(")\n\n");

            for (Contrato c : vencidos) {
                long dias = java.time.temporal.ChronoUnit.DAYS
                        .between(c.getDataFim(), hoje);

                sb.append("• ").append(c.getObjeto())
                  .append("\n   ").append(c.getParceiroNome())
                  .append(" — vencido há ").append(dias).append(" dia(s)\n\n");
            }
        }

        if (!aVencer.isEmpty()) {

            if (!vencidos.isEmpty()) {
                sb.append("\n");
            }

            sb.append("🔔 PRÓXIMOS DO VENCIMENTO (").append(aVencer.size()).append(")\n\n");

            for (Contrato c : aVencer) {
                long dias = java.time.temporal.ChronoUnit.DAYS
                        .between(hoje, c.getDataFim());

                sb.append("• ").append(c.getObjeto())
                  .append("\n   ").append(c.getParceiroNome())
                  .append(" — vence em ").append(dias).append(" dia(s)\n\n");
            }
        }

        String titulo;
        if (!vencidos.isEmpty() && !aVencer.isEmpty()) {
            titulo = "⚠ Atenção: Contratos vencidos e a vencer";
        } else if (!vencidos.isEmpty()) {
            titulo = "⚠ Contratos vencidos";
        } else {
            titulo = "🔔 Contratos próximos do vencimento";
        }

        AlertaUtil.aviso(titulo, sb.toString());
    }
}