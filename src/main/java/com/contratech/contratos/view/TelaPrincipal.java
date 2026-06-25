package com.contratech.contratos.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.contratech.contratos.dao.ContratoDAO;
import com.contratech.contratos.dao.DashboardDAO;
import com.contratech.contratos.dto.DashboardResumo;
import com.contratech.contratos.model.Contrato;
import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.util.ui.AjudaUtil;
import com.contratech.contratos.util.ui.AlertaUtil;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TelaPrincipal {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Stage stage;
    private final Usuario usuarioLogado;
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private static boolean alertaJaMostrado = false;

    public static void resetarAlerta() {
        alertaJaMostrado = false;
    }

    public TelaPrincipal(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {
        DashboardResumo resumo = dashboardDAO.carregarResumo();

        Label lblBemVindo = new Label("Bem-vindo, " + usuarioLogado.getNome());
        lblBemVindo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblBemVindo.setStyle("-fx-text-fill: white;");

        Label lblResumo = new Label("Monitore parceiros, contratos e vencimentos em um unico painel.");
        lblResumo.setStyle("-fx-text-fill: rgba(255,255,255,0.82); -fx-font-size: 13px;");

        Label lblPerfil = new Label(usuarioLogado.getTipoUsuario().toString());
        lblPerfil.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);"
                + "-fx-padding: 6 14;"
                + "-fx-background-radius: 20;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle(
                "-fx-background-color: #e74c3c;"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 8 18;"
                + "-fx-font-weight: bold;"
        );

        btnLogout.setOnAction(e -> {
            if (AlertaUtil.confirmar("Logout", "Deseja sair?")) {
                TelaPrincipal.resetarAlerta();
                new TelaLogin(stage).exibir();
            }
        });

        Button btnAjuda = AjudaUtil.criarBotaoAjuda(stage, usuarioLogado,
                "Tela principal e dashboard",
                () -> new TelaPrincipal(stage, usuarioLogado).exibir());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        VBox blocoTitulo = new VBox(4, lblBemVindo, lblResumo);

        HBox header = new HBox(15, blocoTitulo, lblPerfil, espaco, btnAjuda, btnLogout);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
        );

        HBox indicadores = criarIndicadores(resumo);
        Label lblSecaoIndicadores = criarLabelSecao("Visao geral");

        HBox painelCentral = new HBox(18,
                criarPainelAgrupamento("Contratos por tipo", resumo.getContratosPorTipo()),
                criarPainelAgrupamento("Contratos por status", resumo.getContratosPorStatus()),
                criarPainelVencimentos(resumo.getVencimentosCriticos())
        );
        painelCentral.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(painelCentral.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(painelCentral.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(painelCentral.getChildren().get(2), Priority.ALWAYS);

        HBox acoesRapidas = criarAcoesRapidas(resumo);
        Label lblSecaoAnalise = criarLabelSecao("Analises e prioridades");
        Label lblSecaoAcoes = criarLabelSecao("Acoes rapidas");

        VBox conteudo = new VBox(18,
                lblSecaoIndicadores,
                indicadores,
                lblSecaoAnalise,
                painelCentral,
                lblSecaoAcoes,
                acoesRapidas
        );
        conteudo.setPadding(new Insets(26));
        conteudo.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(conteudo);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent;");

        Label rodapeTxt = new Label("Sistema Contratech - v1.0");
        rodapeTxt.setStyle("-fx-text-fill: #95a5a6;");

        HBox footer = new HBox(rodapeTxt);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scroll);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef3f8, #dce6f1);");

        Scene scene = new Scene(root, 1100, 700);
        AjudaUtil.registrarAtalhoF1(scene, stage, usuarioLogado,
                "Tela principal e dashboard",
                () -> new TelaPrincipal(stage, usuarioLogado).exibir());

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

    private HBox criarIndicadores(DashboardResumo resumo) {
        HBox indicadores = new HBox(14,
                criarIndicador("Parceiros ativos", resumo.getParceirosAtivos(), "#2c3e50", e -> new TelaParceiro(stage, usuarioLogado).exibir()),
                criarIndicador("Contratos cadastrados", resumo.getContratosCadastrados(), "#34495e", e -> new TelaContrato(stage, usuarioLogado, null).exibir()),
                criarIndicador("Contratos ativos", resumo.getContratosAtivos(), "#2e7d32", e -> new TelaContrato(stage, usuarioLogado, "ATIVO").exibir()),
                criarIndicador("Contratos vencidos", resumo.getContratosVencidos(), "#c62828", e -> new TelaContrato(stage, usuarioLogado, "VENCIDOS").exibir()),
                criarIndicador("A vencer em 30 dias", resumo.getContratosAVencer(), "#ef6c00", e -> new TelaContrato(stage, usuarioLogado, "AVENCER").exibir())
        );
        indicadores.setAlignment(Pos.CENTER);
        return indicadores;
    }

    private VBox criarIndicador(String titulo, int valor, String cor, javafx.event.EventHandler<javafx.scene.input.MouseEvent> acao) {
        Label lblValor = new Label(String.valueOf(valor));
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        lblValor.setStyle("-fx-text-fill: " + cor + ";");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitulo.setStyle("-fx-text-fill: #5d6d7e;");

        VBox box = new VBox(6, lblValor, lblTitulo);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        box.setMinWidth(170);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #dbe5ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12,0,0,3);"
        );
        HBox.setHgrow(box, Priority.ALWAYS);
        String baseStyle = box.getStyle() + "-fx-cursor: hand;";
        box.setStyle(baseStyle);
        box.setOnMouseClicked(acao);
        box.setOnMouseEntered(e -> box.setStyle(baseStyle + "-fx-background-color: #f8fbff;"));
        box.setOnMouseExited(e -> box.setStyle(baseStyle));
        return box;
    }

    private VBox criarPainelAgrupamento(String titulo, Map<String, Integer> dados) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        VBox linhas = new VBox(8);
        linhas.setFillWidth(true);

        if (dados.isEmpty()) {
            Label vazio = new Label("Nenhum contrato cadastrado.");
            vazio.setStyle("-fx-text-fill: #7f8c8d;");
            linhas.getChildren().add(vazio);
        } else {
            dados.forEach((chave, total) -> {
                if ("Contratos por status".equals(titulo) && ("ATIVO".equals(chave) || "CONCLUIDO".equals(chave)
                        || "CANCELADO".equals(chave) || "SUSPENSO".equals(chave))) {
                    linhas.getChildren().add(criarLinhaResumo(chave, total, e -> new TelaContrato(stage, usuarioLogado, chave).exibir()));
                } else {
                    linhas.getChildren().add(criarLinhaResumo(chave, total));
                }
            });
        }

        VBox painel = new VBox(12, lblTitulo, new Separator(), linhas);
        painel.setPadding(new Insets(16));
        painel.setMinHeight(230);
        painel.setMaxWidth(Double.MAX_VALUE);
        painel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #dbe5ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12,0,0,3);"
        );
        return painel;
    }

    private HBox criarLinhaResumo(String texto, int total) {
        Label lblTexto = new Label(texto);
        lblTexto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTexto.setStyle("-fx-text-fill: #34495e;");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Label lblTotal = new Label(String.valueOf(total));
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTotal.setStyle(
                "-fx-background-color: #eef3f8;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 4 10;"
                + "-fx-text-fill: #2c3e50;"
        );

        HBox linha = new HBox(10, lblTexto, espaco, lblTotal);
        linha.setAlignment(Pos.CENTER_LEFT);
        return linha;
    }

    private HBox criarLinhaResumo(String texto, int total, javafx.event.EventHandler<javafx.scene.input.MouseEvent> acao) {
        HBox linha = criarLinhaResumo(texto, total);
        linha.setOnMouseClicked(acao);
        linha.setStyle(linha.getStyle() + "-fx-cursor: hand;");
        linha.setOnMouseEntered(e -> linha.setStyle(linha.getStyle() + "-fx-background-color: #f8fbff;"));
        linha.setOnMouseExited(e -> linha.setStyle(linha.getStyle().replace("-fx-background-color: #f8fbff;", "")));
        return linha;
    }

    private VBox criarPainelVencimentos(List<Contrato> contratos) {
        Label lblTitulo = new Label("Vencimentos críticos");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #2c3e50;");

        VBox lista = new VBox(8);
        lista.setFillWidth(true);

        if (contratos.isEmpty()) {
            Label vazio = new Label("Nenhum contrato vencido ou a vencer em 30 dias.");
            vazio.setWrapText(true);
            vazio.setStyle("-fx-text-fill: #7f8c8d;");
            lista.getChildren().add(vazio);
        } else {
            contratos.stream()
                    .limit(6)
                    .forEach(c -> lista.getChildren().add(criarLinhaVencimento(c)));
        }

        VBox painel = new VBox(12, lblTitulo, new Separator(), lista);
        painel.setPadding(new Insets(16));
        painel.setMinHeight(230);
        painel.setMaxWidth(Double.MAX_VALUE);
        painel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #dbe5ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12,0,0,3);"
        );
        return painel;
    }

    private GridPane criarLinhaVencimento(Contrato contrato) {
        Label lblContrato = new Label(contrato.getNumeroContrato() + " - " + contrato.getObjeto());
        lblContrato.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblContrato.setStyle("-fx-text-fill: #34495e;");
        lblContrato.setWrapText(true);

        Label lblParceiro = new Label(contrato.getParceiroNome());
        lblParceiro.setStyle("-fx-text-fill: #7f8c8d;");
        lblParceiro.setWrapText(true);

        Label lblData = new Label(contrato.getDataFim() != null ? contrato.getDataFim().format(FMT) : "-");
        lblData.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblData.setStyle("-fx-text-fill: #2c3e50;");

        Label lblSituacao = new Label(formatarSituacao(contrato));
        lblSituacao.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblSituacao.setStyle(
                "-fx-background-color: " + corSituacao(contrato) + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 4 10;"
                + "-fx-text-fill: white;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(3);
        grid.add(lblContrato, 0, 0);
        grid.add(lblData, 1, 0);
        grid.add(lblParceiro, 0, 1);
        grid.add(lblSituacao, 1, 1);
        GridPane.setHgrow(lblContrato, Priority.ALWAYS);
        GridPane.setHgrow(lblParceiro, Priority.ALWAYS);
        grid.setStyle("-fx-border-color: transparent transparent #edf1f5 transparent; -fx-padding: 0 0 8 0;");
        return grid;
    }

    private String formatarSituacao(Contrato contrato) {
        LocalDate hoje = LocalDate.now();
        LocalDate vencimento = contrato.getDataFim();

        if (vencimento == null) {
            return "Sem data";
        }

        if (vencimento.isBefore(hoje)) {
            long dias = ChronoUnit.DAYS.between(vencimento, hoje);
            return "Vencido ha " + dias + " dia(s)";
        }

        long dias = ChronoUnit.DAYS.between(hoje, vencimento);
        return "Vence em " + dias + " dia(s)";
    }

    private String corSituacao(Contrato contrato) {
        return contrato.getDataFim() != null && contrato.getDataFim().isBefore(LocalDate.now())
                ? "#c62828"
                : "#ef6c00";
    }

    private HBox criarAcoesRapidas(DashboardResumo resumo) {
        Button btnUsuarios = criarCard("U", "Usuarios");
        Button btnParceiros = criarCard("P", "Parceiros");
        Button btnContratos = criarCard("C", "Contratos (" + (resumo.getContratosVencidos() + resumo.getContratosAVencer()) + ")");

        btnUsuarios.setOnAction(e -> {
            if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.ADMIN) {
                new TelaUsuario(stage, usuarioLogado).exibir();
            } else {
                AlertaUtil.erro("Acesso negado", "Somente ADMIN");
            }
        });

        btnParceiros.setOnAction(e -> new TelaParceiro(stage, usuarioLogado).exibir());
        btnContratos.setOnAction(e -> new TelaContrato(stage, usuarioLogado, null).exibir());

        if (usuarioLogado.getTipoUsuario() != Usuario.TipoUsuario.ADMIN) {
            btnUsuarios.setOpacity(0.5);
            btnUsuarios.setTooltip(new Tooltip("Somente ADMIN"));
        }

        HBox menu = new HBox(28, btnUsuarios, btnParceiros, btnContratos);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(10, 0, 10, 0));
        return menu;
    }

    private Button criarCard(String sigla, String texto) {
        Button btn = new Button();
        btn.setPrefSize(220, 135);

        Label icon = new Label(sigla);
        icon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        icon.setStyle("-fx-text-fill: #4ca1af;");

        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        VBox box = new VBox(10, icon, titulo);
        box.setAlignment(Pos.CENTER);

        btn.setGraphic(box);

        String base =
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f5f7fa);"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: rgba(0,0,0,0.06);"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10,0,0,3);"
                + "-fx-cursor: hand;";

        String hover =
                "-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);"
                + "-fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 18,0,0,6);"
                + "-fx-cursor: hand;";

        btn.setStyle(base);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hover);
            titulo.setStyle("-fx-text-fill: white;");
            icon.setStyle("-fx-text-fill: white;");

            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToY(-5);
            tt.play();

            ScaleTransition st = new ScaleTransition(Duration.millis(160), btn);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(base);
            titulo.setStyle("-fx-text-fill: #2c3e50;");
            icon.setStyle("-fx-text-fill: #4ca1af;");

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

    private Label criarLabelSecao(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #4ca1af; -fx-padding: 4 0 0 2;");
        return label;
    }

    private void verificarContratosVencidos() {
        ContratoDAO contratoDAO = new ContratoDAO();
        List<Contrato> contratos = contratoDAO.listarContratosVencidos();

        if (contratos.isEmpty()) {
            return;
        }

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

        if (contratos.size() == 1) {
            Contrato c = contratos.get(0);

            if (c.getDataFim().isBefore(hoje)) {
                long dias = ChronoUnit.DAYS.between(c.getDataFim(), hoje);

                String mensagem = "CONTRATO VENCIDO\n\n"
                        + c.getObjeto() + "\n"
                        + c.getParceiroNome() + "\n\n"
                        + "Vencido ha " + dias + " dia(s)";

                AlertaUtil.aviso("Atencao", mensagem);
            } else {
                long dias = ChronoUnit.DAYS.between(hoje, c.getDataFim());

                String mensagem = "CONTRATO PROXIMO DO VENCIMENTO\n\n"
                        + c.getObjeto() + "\n"
                        + c.getParceiroNome() + "\n\n"
                        + "Vence em " + dias + " dia(s)";

                AlertaUtil.aviso("Aviso", mensagem);
            }

            return;
        }

        int qtdVencidos = vencidos.size();
        int qtdAVencer = aVencer.size();

        StringBuilder sb = new StringBuilder();
        sb.append("ALERTA DE CONTRATOS\n\n");

        if (qtdVencidos > 0) {
            sb.append(qtdVencidos).append(" contrato(s) vencido(s)\n");
        }

        if (qtdAVencer > 0) {
            sb.append(qtdAVencer).append(" contrato(s) proximo(s) ao vencimento\n");
        }

        String titulo;

        if (qtdVencidos > 0 && qtdAVencer > 0) {
            titulo = "Situacao dos Contratos";
        } else if (qtdVencidos > 0) {
            titulo = "Contratos Vencidos";
        } else {
            titulo = "Proximos do Vencimento";
        }

        AlertaUtil.aviso(titulo, sb.toString());
    }
}
