package com.contratech.cadastrocontrato.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.contratech.cadastrocontrato.dao.ContratoDAO;
import com.contratech.cadastrocontrato.model.Contrato;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Tela principal do sistema com menu de navegação.
 * 
 * Funcionalidades:
 * - Menu com acesso a Usuários, Parceiros e Contratos
 * - Alerta de contratos vencidos ao abrir
 * - Controle de acesso baseado no tipo de usuário
 */
public class TelaPrincipal {

    private final Stage stage;
    private final Usuario usuarioLogado;

    public TelaPrincipal(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {
        // === Barra superior ===
        Label lblBemVindo = new Label("Bem-vindo, " + usuarioLogado.getNome() + "!");
        lblBemVindo.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label lblPerfil = new Label("Perfil: " + usuarioLogado.getTipoUsuario());
        lblPerfil.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> {
            if (AlertaUtil.confirmar("Logout", "Deseja realmente sair do sistema?")) {
                TelaLogin telaLogin = new TelaLogin(stage);
                telaLogin.exibir();
            }
        });

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        HBox barraSuperior = new HBox(15, lblBemVindo, lblPerfil, espacador, btnLogout);
        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setPadding(new Insets(15, 20, 15, 20));
        barraSuperior.setStyle("-fx-background-color: white; "
                             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // === Botões do menu ===
        Button btnUsuarios = criarBotaoMenu("Usuários", "#2196F3",
                "Cadastro, edição e exclusão de usuários do sistema");
        Button btnParceiros = criarBotaoMenu("Parceiros", "#4CAF50",
                "Cadastro de clientes e fornecedores");
        Button btnContratos = criarBotaoMenu("Contratos", "#FF9800",
                "Gestão de contratos e cláusulas");

        // Ações dos botões
        btnUsuarios.setOnAction(e -> {
            // Checa permissão no momento do clique e registra tentativas negadas
            if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.ADMIN) {
                TelaUsuario telaUsuario = new TelaUsuario(stage, usuarioLogado);
                telaUsuario.exibir();
            } else {
                com.contratech.cadastrocontrato.util.AuditUtil.logDeniedAccess(usuarioLogado,
                        "Abrir TelaUsuarios", "Permissão insuficiente");
                AlertaUtil.erro("Acesso negado", "Acesso restrito: somente ADMIN pode acessar Usuários.");
            }
        });

        btnParceiros.setOnAction(e -> {
            TelaParceiro telaParceiro = new TelaParceiro(stage, usuarioLogado);
            telaParceiro.exibir();
        });

        btnContratos.setOnAction(e -> {
            TelaContrato telaContrato = new TelaContrato(stage, usuarioLogado);
            telaContrato.exibir();
        });

        // Mantemos indicação visual para quem não tem acesso
        if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.VISUALIZADOR
                || usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.RESPONSAVEL) {
            btnUsuarios.setStyle(btnUsuarios.getStyle() + "-fx-opacity: 0.6;");
            btnUsuarios.setTooltip(new javafx.scene.control.Tooltip("Somente ADMIN pode acessar"));
        }

        // === Painel central com os botões ===
        HBox painelMenu = new HBox(20, btnUsuarios, btnParceiros, btnContratos);
        painelMenu.setAlignment(Pos.CENTER);
        painelMenu.setPadding(new Insets(30));
        painelMenu.setFillHeight(true);
        painelMenu.setPrefWidth(Double.MAX_VALUE);
        painelMenu.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(painelMenu, Priority.ALWAYS);

        // === Rodapé ===
        Label lblRodape = new Label("CadastroContrato v1.0 | Grupo Tríade");
        lblRodape.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        HBox rodape = new HBox(lblRodape);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(10));

        // === Layout principal ===
        BorderPane root = new BorderPane();
        root.setTop(barraSuperior);
        root.setCenter(painelMenu);
        root.setBottom(rodape);
        root.setStyle("-fx-background-color: #ECEFF1;");

        Scene scene = new Scene(root, 750, 450);
        stage.setTitle("CadastroContrato - Menu Principal");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);

        // Verifica contratos vencidos ao abrir a tela
        verificarContratosVencidos();
    }

    /**
     * Cria um botão estilizado para o menu principal.
     */
    private Button criarBotaoMenu(String texto, String cor, String descricao) {
        Button btn = new Button(texto);
        btn.setPrefWidth(180);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(100);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; "
                   + "-fx-background-radius: 8; -fx-cursor: hand;");
        HBox.setHgrow(btn, Priority.ALWAYS);

        // Tooltip com descrição ao passar o mouse
        btn.setTooltip(new Tooltip(descricao));

        return btn;
    }

    /**
     * Consulta contratos vencidos e exibe alerta se houver.
     * Roda toda vez que o usuário abre ou volta pra tela principal.
     */
    private void verificarContratosVencidos() {
    ContratoDAO contratoDAO = new ContratoDAO();
    List<Contrato> contratos = contratoDAO.listarVencidos();

    if (contratos.isEmpty()) {
        return;
    }

    LocalDate hoje = LocalDate.now();

    // Separa vencidos dos que estão próximos do vencimento
    List<Contrato> vencidos = new ArrayList<>();
    List<Contrato> aVencer = new ArrayList<>();

    for (Contrato c : contratos) {
        if (c.getDataFim() != null && c.getDataFim().isBefore(hoje)) {
            vencidos.add(c);
        } else {
            aVencer.add(c);
        }
    }

    // Monta a mensagem
    StringBuilder sb = new StringBuilder();

    if (!vencidos.isEmpty()) {
        sb.append("⚠ ").append(vencidos.size()).append(" contrato(s) VENCIDO(S):\n\n");
        for (Contrato c : vencidos) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(c.getDataFim(), hoje);
            sb.append("• ").append(c.getObjeto())
              .append(" (").append(c.getParceiroNome()).append(")")
              .append(" — vencido há ").append(dias).append(" dia(s)\n");
        }
    }

    if (!aVencer.isEmpty()) {
        if (!vencidos.isEmpty()) {
            sb.append("\n");
        }
        sb.append("🔔 ").append(aVencer.size()).append(" contrato(s) PRÓXIMO(S) DO VENCIMENTO:\n\n");
        for (Contrato c : aVencer) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(hoje, c.getDataFim());
            sb.append("• ").append(c.getObjeto())
              .append(" (").append(c.getParceiroNome()).append(")")
              .append(" — vence em ").append(dias).append(" dia(s)\n");
        }
    }

    // Define o título do alerta conforme a situação
    String titulo;
    if (!vencidos.isEmpty() && !aVencer.isEmpty()) {
        titulo = "Contratos Vencidos e a Vencer";
    } else if (!vencidos.isEmpty()) {
        titulo = "Contratos Vencidos";
    } else {
        titulo = "Contratos Próximos do Vencimento";
    }

    AlertaUtil.aviso(titulo, sb.toString());
    }

}
