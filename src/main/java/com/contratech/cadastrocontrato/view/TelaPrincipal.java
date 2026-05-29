package com.contratech.cadastrocontrato.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.contratech.cadastrocontrato.dao.ContratoDAO;
import com.contratech.cadastrocontrato.model.Contrato;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

public class TelaPrincipal {

    private final Stage stage;
    private final Usuario usuarioLogado;
    private static boolean alertaJaMostrado = false;

    public static void resetarAlerta() {
        alertaJaMostrado = false;
    }

    public TelaPrincipal(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {

        // ===== HEADER =====
        Label lblBemVindo = new Label("Bem-vindo, " + usuarioLogado.getNome());
        lblBemVindo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblBemVindo.setStyle("-fx-text-fill: white;");

        Label lblPerfil = new Label(usuarioLogado.getTipoUsuario().toString());
        lblPerfil.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-padding: 6 14;" +
                "-fx-background-radius: 20;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );
        int totalAlerta = new ContratoDAO().listarVencidos().size();

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle(
                "-fx-background-color: #e74c3c;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 8 18;" +
                "-fx-font-weight: bold;"
        );

        btnLogout.setOnAction(e -> {
            if (AlertaUtil.confirmar("Logout", "Deseja sair?")) {
                TelaPrincipal.resetarAlerta();
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

        // ===== BOTÕES =====
        Button btnUsuarios = criarCard("👤", "Usuários");
        Button btnParceiros = criarCard("🤝", "Parceiros");
        Button btnContratos = criarCard("📄", "Contratos (" + totalAlerta + ")");


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

        HBox menu = new HBox(60, btnUsuarios, btnParceiros, btnContratos);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(90));

        // ===== FOOTER =====
        Label rodapeTxt = new Label("Sistema Contratech • v1.0");
        rodapeTxt.setStyle("-fx-text-fill: #95a5a6;");

        HBox footer = new HBox(rodapeTxt);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));

        // ===== ROOT =====
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(menu);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef3f8, #dce6f1);");

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

        if (!alertaJaMostrado) {
            verificarContratosVencidos();
            alertaJaMostrado = true;
        }
    }

    // ===== CARD PREMIUM =====
    private Button criarCard(String emoji, String texto) {

        Button btn = new Button();
        btn.setPrefSize(250, 160);

        // ===== EMOJI GRANDE =====
        Label icon = new Label(emoji);
        icon.setFont(Font.font(42));

        // ===== TEXTO =====
        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        VBox box = new VBox(12, icon, titulo);
        box.setAlignment(Pos.CENTER);

        btn.setGraphic(box);

        // ===== ESTILO BASE =====
        String base =
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f5f7fa);" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: rgba(0,0,0,0.06);" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10,0,0,3);" +
                "-fx-cursor: hand;";

        // ===== HOVER BONITO (SEM EXAGERO) =====
        String hover =
                "-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 18,0,0,6);" +
                "-fx-cursor: hand;";

        btn.setStyle(base);

        btn.setOnMouseEntered(e -> {

            btn.setStyle(hover);

            titulo.setStyle("-fx-text-fill: white;");

            // animação suave (sem exagero)
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToY(-5);
            tt.play();

            ScaleTransition st = new ScaleTransition(Duration.millis(160), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        btn.setOnMouseExited(e -> {

            btn.setStyle(base);

            titulo.setStyle("-fx-text-fill: #2c3e50;");

            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToY(0);
            tt.play();

            ScaleTransition st = new ScaleTransition(Duration.millis(160), btn);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });

        return btn;
    }



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

    String mensagem = "";

    // ===== CASO: APENAS 1 CONTRATO =====
    if (contratos.size() == 1) {

        Contrato c = contratos.get(0);

        if (c.getDataFim().isBefore(hoje)) {

            long dias = java.time.temporal.ChronoUnit.DAYS
                    .between(c.getDataFim(), hoje);

            mensagem =
                    "🔴 CONTRATO VENCIDO\n\n" +
                    "📄 " + c.getObjeto() + "\n" +
                    "👥 " + c.getParceiroNome() + "\n\n" +
                    "⏱ Vencido há " + dias + " dia(s)";
                    
            AlertaUtil.aviso("⚠ Atenção", mensagem);

        } else {

            long dias = java.time.temporal.ChronoUnit.DAYS
                    .between(hoje, c.getDataFim());

            mensagem =
                    "🟡 CONTRATO PRÓXIMO DO VENCIMENTO\n\n" +
                    "📄 " + c.getObjeto() + "\n" +
                    "👥 " + c.getParceiroNome() + "\n\n" +
                    "⏳ Vence em " + dias + " dia(s)";

            AlertaUtil.aviso("🔔 Aviso", mensagem);
        }

        return;
    }

    // ===== CASO: VÁRIOS CONTRATOS =====
    int qtdVencidos = vencidos.size();
    int qtdAVencer = aVencer.size();

    StringBuilder sb = new StringBuilder();

    sb.append(" ALERTA DE CONTRATOS\n\n");

    if (qtdVencidos > 0) {
        sb.append("🔴 ").append(qtdVencidos).append(" contrato(s) vencido(s)\n");
    }

    if (qtdAVencer > 0) {
        sb.append("🟡 ").append(qtdAVencer).append(" contrato(s) próximo(s) ao vencimento\n");
    }


    String titulo;

    if (qtdVencidos > 0 && qtdAVencer > 0) {
        titulo = "⚠ Situação dos Contratos";
    } else if (qtdVencidos > 0) {
        titulo = "⚠ Contratos Vencidos";
    } else {
        titulo = "🔔 Próximos do Vencimento";
    }

    AlertaUtil.aviso(titulo, sb.toString());
}

}
