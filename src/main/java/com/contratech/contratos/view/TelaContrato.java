package com.contratech.contratos.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.contratech.contratos.dao.ClausulaDAO;
import com.contratech.contratos.dao.ContratoDAO;
import com.contratech.contratos.dao.ParceiroDAO;
import com.contratech.contratos.model.Clausula;
import com.contratech.contratos.model.Contrato;
import com.contratech.contratos.model.Parceiro;
import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.util.ui.AjudaUtil;
import com.contratech.contratos.util.ui.AlertaUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Tela de Gestão de Contratos com painel integrado de Cláusulas.
 *
 * Fluxo: o usuário cadastra/seleciona um contrato → o painel de cláusulas
 * é desbloqueado e exibe as cláusulas vinculadas àquele contrato.
 * Isso elimina a troca de contexto entre telas.
 */
public class TelaContrato {

    private final Stage stage;
    private final Usuario usuarioLogado;
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final ParceiroDAO parceiroDAO = new ParceiroDAO();
    private final ClausulaDAO clausulaDAO = new ClausulaDAO();
    private final String filtroInicial;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== CAMPOS DO CONTRATO ====================
    private TextField txtNumeroContrato;
    private ComboBox<Parceiro> cbParceiro;
    private TextField txtObjeto;
    private TextArea txtDescricao;
    private ComboBox<String> cbTipo;
    private TextField txtValor;
    private TextField txtMulta;
    private ComboBox<String> cbFormaPagamento;
    private DatePicker txtDataInicio;
    private DatePicker txtDataFim;
    private ComboBox<String> cbStatus;
    private TextArea txtObservacoes;
    private TextField txtPesquisa;
    private Button btnFiltroTodos;
    private Button btnFiltroAtivos;
    private Button btnFiltroConcluidos;
    private Button btnFiltroSuspensos;
    private Button btnFiltroCancelados;
    private Button btnFiltroVencidos;
    private Button btnFiltroAVencer;
    private String filtroAtual = "TODOS";

    // ==================== TABELA DE CONTRATOS ====================
    private TableView<Contrato> tabelaContratos;
    private ObservableList<Contrato> listaContratos;

    // ==================== CAMPOS DAS CLÁUSULAS ====================
    private TextField txtClausulaNumero;
    private TextArea txtClausulaDescricao;
    private TableView<Clausula> tabelaClausulas;
    private ObservableList<Clausula> listaClausulas;
    private VBox painelClausulas;
    private StackPane painelTabelaStack;
    private StackPane overlayVisualizacaoContrato;
    private Label lblVisualizacaoTitulo;
    private Label lblVisualizacaoResumo;
    private VBox boxDetalhesVisualizacao;
    private VBox boxClausulasVisualizacao;
    private Label lblResumoContrato;
    private Label lblChecklistContrato;
    private Label lblResumoClausulas;

    // ==================== CONTROLE DE ESTADO ====================
    private Contrato contratoSelecionado = null;
    private Clausula clausulaSelecionada = null;

    public TelaContrato(Stage stage, Usuario usuarioLogado) {
        this(stage, usuarioLogado, null);
    }

    public TelaContrato(Stage stage, Usuario usuarioLogado, String filtroInicial) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
        this.filtroInicial = filtroInicial;
    }

    public void exibir() {

    // ===== HEADER PADRÃO =====
    Label lblTitulo = new Label("Contratos  |  " + usuarioLogado.getNome());
    lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
    lblTitulo.setStyle("-fx-text-fill: white;");

    Label lblResumo = new Label("Organize cadastros, acompanhe vigencias e mantenha clausulas vinculadas ao contrato.");
    lblResumo.setStyle("-fx-text-fill: rgba(255,255,255,0.82); -fx-font-size: 13px;");

    Label lblPerfil = new Label(usuarioLogado.getTipoUsuario().toString());
    lblPerfil.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-padding: 5 10;" +
            "-fx-background-radius: 20;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
    );

    Button btnMenu = new Button("🏠 Menu");
    btnMenu.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 6 14;" +
            "-fx-font-weight: bold;"
    );
    btnMenu.setOnAction(e -> new TelaPrincipal(stage, usuarioLogado).exibir());

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

    Button btnAjuda = AjudaUtil.criarBotaoAjuda(stage, usuarioLogado,
            "Cadastro de contratos",
            () -> new TelaContrato(stage, usuarioLogado).exibir());

    Region espacador = new Region();
    HBox.setHgrow(espacador, Priority.ALWAYS);

    VBox blocoTitulo = new VBox(4, lblTitulo, lblResumo);

    HBox header = new HBox(12, blocoTitulo, lblPerfil, espacador, btnAjuda, btnMenu, btnLogout);
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(18, 20, 18, 20));
    header.setPrefHeight(78);
    header.setMinHeight(78);
    header.setMaxHeight(78);
    header.setStyle(
            "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
    );

    // ===== FORMULÁRIO =====
    VBox formContrato = criarFormularioContrato();


    // ===== CLÁUSULAS VISÍVEL (FIXO) =====
    painelClausulas = criarPainelClausulas();

    if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.VISUALIZADOR) {
        formContrato.setDisable(true);
        painelClausulas.setDisable(true);
    }

    // ===== TABELA =====
    StackPane tabela = criarPainelTabelaContratos(formContrato, painelClausulas);

    VBox centro = new VBox(tabela);
    centro.setPadding(new Insets(20));
    VBox.setVgrow(tabela, Priority.ALWAYS);

    // ===== ROOT =====
    BorderPane root = new BorderPane();
    root.setTop(header);
    root.setCenter(centro);
    root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef3f8, #dce6f1);");

    Scene scene = new Scene(root, 1200, 700);
    AjudaUtil.registrarAtalhoF1(scene, stage, usuarioLogado,
            "Cadastro de contratos",
            () -> new TelaContrato(stage, usuarioLogado).exibir());

    stage.setScene(scene);
    stage.setTitle("Contratos");
    stage.setMaximized(true);
    stage.show();

        if (filtroInicial == null) {
            atualizarTabelaContratos();
        } else {
            aplicarFiltroInicial();
        }
        atualizarEstadoClausulas();
        atualizarResumoContrato();
}

    private void aplicarFiltroInicial() {
        if (filtroInicial == null) {
            atualizarTabelaContratos();
            return;
        }

        switch (filtroInicial) {
            case "ATIVO" -> listaContratos.setAll(contratoDAO.listarPorStatus("ATIVO"));
            case "CONCLUIDO" -> listaContratos.setAll(contratoDAO.listarPorStatus("CONCLUIDO"));
            case "CANCELADO" -> listaContratos.setAll(contratoDAO.listarPorStatus("CANCELADO"));
            case "SUSPENSO" -> listaContratos.setAll(contratoDAO.listarPorStatus("SUSPENSO"));
            case "VENCIDOS" -> listaContratos.setAll(contratoDAO.listarContratosVencidos());
            case "AVENCER" -> listaContratos.setAll(contratoDAO.listarContratosAVencer());
            default -> atualizarTabelaContratos();
        }

        txtPesquisa.clear();
    }

    private VBox criarFormularioContrato() {
        Label lblForm = new Label("Dados do Contrato");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        lblResumoContrato = criarResumoPainel(
                "Novo contrato",
                "Preencha os campos principais e selecione um registro na tabela quando quiser editar."
        );
        lblChecklistContrato = new Label();
        lblChecklistContrato.setWrapText(true);

        // --- Nº Contrato ---
        txtNumeroContrato = new TextField();
        txtNumeroContrato.setPromptText("Ex: 2026/001");
        txtNumeroContrato.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtNumeroContrato);

        // --- Parceiro ---
        cbParceiro = new ComboBox<>();
        cbParceiro.setPromptText("Selecione...");
        cbParceiro.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(cbParceiro);
        carregarParceiros();

        // --- Objeto ---
        txtObjeto = new TextField();
        txtObjeto.setPromptText("Objeto do contrato");
        txtObjeto.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtObjeto);

        // --- Descrição ---
        txtDescricao = new TextArea();
        txtDescricao.setPromptText("Descrição detalhada...");
        txtDescricao.setPrefRowCount(3);
        txtDescricao.setWrapText(true);
        txtDescricao.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtDescricao);

        // --- Tipo ---
        cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "SERVICO", "FORNECIMENTO", "MISTO", "LOCACAO", "CONSULTORIA"));
        cbTipo.setPromptText("Tipo");
        cbTipo.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(cbTipo);

        // --- Valor e Multa ---
        txtValor = new TextField();
        txtValor.setPromptText("0.00");
        txtValor.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtValor);
        aplicarMascaraMonetaria(txtValor);

        txtMulta = new TextField();
        txtMulta.setPromptText("0.00");
        txtMulta.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtMulta);
        aplicarMascaraMonetaria(txtMulta);

        HBox hbValores = new HBox(10);
        VBox vbValor = new VBox(2, new Label("Valor (R$):"), txtValor);
        VBox vbMulta = new VBox(2, new Label("Multa (R$):"), txtMulta);
        HBox.setHgrow(vbValor, Priority.ALWAYS);
        HBox.setHgrow(vbMulta, Priority.ALWAYS);
        hbValores.getChildren().addAll(vbValor, vbMulta);

        // --- Forma de Pagamento ---
        cbFormaPagamento = new ComboBox<>(FXCollections.observableArrayList(
                "A_VISTA", "PARCELADO", "MENSAL", "RECORRENTE"));
        cbFormaPagamento.setPromptText("Forma de Pagamento");
        cbFormaPagamento.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(cbFormaPagamento);

        // --- Datas ---
        txtDataInicio = criarDatePicker();
        txtDataInicio.setPromptText("dd/MM/yyyy");
        txtDataInicio.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtDataInicio);

        txtDataFim = criarDatePicker();
        txtDataFim.setPromptText("dd/MM/yyyy");
        txtDataFim.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtDataFim);

        HBox hbDatas = new HBox(10);
        VBox vbInicio = new VBox(2, new Label("Data Início:"), txtDataInicio);
        VBox vbFim = new VBox(2, new Label("Data Fim:"), txtDataFim);
        HBox.setHgrow(vbInicio, Priority.ALWAYS);
        HBox.setHgrow(vbFim, Priority.ALWAYS);
        hbDatas.getChildren().addAll(vbInicio, vbFim);

        // --- Status ---
        cbStatus = new ComboBox<>(FXCollections.observableArrayList(
                "ATIVO", "CONCLUIDO", "CANCELADO", "SUSPENSO"));
        cbStatus.setPromptText("Status");
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(cbStatus);

        // --- Observações ---
        txtObservacoes = new TextArea();
        txtObservacoes.setPromptText("Observações opcionais...");
        txtObservacoes.setPrefRowCount(2);
        txtObservacoes.setWrapText(true);
        txtObservacoes.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtObservacoes);
        configurarChecklistContrato();

        // === Botões ===
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setPrefWidth(130);
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setStyle(estiloBotaoPrimario());
        btnSalvar.setOnAction(e -> salvarContrato());

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setPrefWidth(130);
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle(estiloBotaoDestrutivo());
        btnExcluir.setOnAction(e -> excluirContrato());

        HBox botoesAcao = new HBox(10, btnSalvar, btnExcluir);
        botoesAcao.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSalvar, Priority.ALWAYS);
        HBox.setHgrow(btnExcluir, Priority.ALWAYS);

        Button btnLimpar = new Button("Novo Contrato");
        btnLimpar.setPrefWidth(270);
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setStyle(estiloBotaoSecundario());
        btnLimpar.setOnAction(e -> limparFormularioContrato());

        // === Monta ===
        VBox form = new VBox(8);
        form.setPadding(new Insets(15));
        form.setMinWidth(340);
        form.setMaxWidth(Double.MAX_VALUE);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(20,35,50,0.10), 14,0,0,4);");

        form.getChildren().addAll(
                lblForm,
                lblResumoContrato,
                lblChecklistContrato,
                new Separator(),
                criarTituloSecao("Dados do contrato"),
                criarTextoApoio("Comece por numero, parceiro e objeto. Os demais campos refinam o contrato."),
                new Label("Nº Contrato:"), txtNumeroContrato,
                new Label("Parceiro:"), cbParceiro,
                new Label("Objeto:"), txtObjeto,
                new Label("Descrição:"), txtDescricao,
                new Label("Tipo:"), cbTipo,
                criarTituloSecao("Prazos e valores"),
                hbValores,
                new Label("Forma de Pagamento:"), cbFormaPagamento,
                hbDatas,
                criarTituloSecao("Situacao"),
                new Label("Status:"), cbStatus,
                new Label("Observações:"), txtObservacoes,
                new Separator(),
                botoesAcao,
                btnLimpar
        );

        return form;
    }

    // ===================================================================
    // PAINEL DE CLÁUSULAS (integrado abaixo do formulário)
    // ===================================================================

    private VBox criarPainelClausulas() {
        Label lblTitulo = new Label("Cláusulas do Contrato");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        lblResumoClausulas = criarResumoPainel(
                "Clausulas bloqueadas",
                "Selecione um contrato na tabela para cadastrar e revisar as clausulas vinculadas."
        );

        // --- Nº da Cláusula ---
        txtClausulaNumero = new TextField();
        txtClausulaNumero.setPromptText("Nº");
        txtClausulaNumero.setPrefWidth(60);
        estilizarCampo(txtClausulaNumero);

        // Permite apenas números
        txtClausulaNumero.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtClausulaNumero.setText(oldVal);
            }
        });

        // --- Descrição da Cláusula ---
        txtClausulaDescricao = new TextArea();
        txtClausulaDescricao.setPromptText("Texto da cláusula...");
        txtClausulaDescricao.setPrefRowCount(3);
        txtClausulaDescricao.setWrapText(true);
        estilizarCampo(txtClausulaDescricao);

        // --- Botões das cláusulas ---
        Button btnAddClausula = new Button("Salvar Cláusula");
        btnAddClausula.setStyle(estiloBotaoPrimario());
        btnAddClausula.setMaxWidth(Double.MAX_VALUE);
        btnAddClausula.setOnAction(e -> salvarClausula());

        Button btnExcClausula = new Button("Excluir Cláusula");
        btnExcClausula.setStyle(estiloBotaoDestrutivo());
        btnExcClausula.setMaxWidth(Double.MAX_VALUE);
        btnExcClausula.setOnAction(e -> excluirClausula());

        Button btnLimparClausula = new Button("Limpar");
        btnLimparClausula.setStyle(estiloBotaoSecundario());
        btnLimparClausula.setMaxWidth(Double.MAX_VALUE);
        btnLimparClausula.setOnAction(e -> limparFormularioClausula());

        HBox botoesClausula = new HBox(5, btnAddClausula, btnExcClausula, btnLimparClausula);

        // --- Mini-tabela de cláusulas ---
        tabelaClausulas = new TableView<>();
        listaClausulas = FXCollections.observableArrayList();
        tabelaClausulas.setItems(listaClausulas);
        tabelaClausulas.setPrefHeight(180);
        tabelaClausulas.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabelaClausulas, Priority.ALWAYS);
        tabelaClausulas.setPlaceholder(criarResumoPainel(
                "Nenhuma clausula cadastrada",
                "Use o formulario acima para incluir a primeira clausula deste contrato."
        ));

        configurarColunasClausulas();

        tabelaClausulas.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) {
                        preencherFormularioClausula(novo);
                    }
                }
        );

        // === Monta o painel ===
        VBox painel = new VBox(8);
        painel.setPadding(new Insets(12));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14; -fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(20,35,50,0.08), 12,0,0,3);");
        painel.setMaxWidth(Double.MAX_VALUE);

        painel.getChildren().addAll(
                lblTitulo,
                lblResumoClausulas,
                new Separator(),
                new Label("Nº Cláusula:"), txtClausulaNumero,
                new Label("Descrição:"), txtClausulaDescricao,
                botoesClausula,
                tabelaClausulas
        );

        return painel;
    }

    private void configurarColunasClausulas() {
        TableColumn<Clausula, Integer> colNum = new TableColumn<>("Nº");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNum.setPrefWidth(40);

        TableColumn<Clausula, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colDesc.setPrefWidth(250);

        // Trunca textos longos na mini-tabela
        colDesc.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.length() > 80 ? item.substring(0, 80) + "..." : item);
                }
            }
        });

        java.util.List<TableColumn<Clausula, ?>> colunasClausulas = new java.util.ArrayList<>();
        colunasClausulas.add(colNum);
        colunasClausulas.add(colDesc);
        tabelaClausulas.getColumns().addAll(colunasClausulas);
    }

    // ===================================================================
    // TABELA DE CONTRATOS (direita)
    // ===================================================================

    private StackPane criarPainelTabelaContratos(VBox formContrato, VBox painelClausulas) {
        txtPesquisa = new TextField();
        estilizarCampo(txtPesquisa);
        txtPesquisa.setPromptText("Pesquisar por nº contrato, objeto ou parceiro...");

        Button btnPesquisar = new Button("Pesquisar");
        btnPesquisar.setStyle(estiloBotaoPrimario());
        btnPesquisar.setOnAction(e -> pesquisarContratos());

        Button btnListarTodos = new Button("Listar todos");
        btnListarTodos.setStyle(estiloBotaoSecundario());
        btnListarTodos.setOnAction(e -> atualizarTabelaContratos());

        Button btnNovoContrato = new Button("Novo contrato");
        btnNovoContrato.setStyle(estiloBotaoPrimario());
        btnNovoContrato.setOnAction(e -> {
            limparFormularioContrato();
            abrirVisualizacaoContrato(null);
        });

        txtPesquisa.setOnAction(e -> pesquisarContratos());

        HBox barraPesquisa = new HBox(10, txtPesquisa, btnPesquisar, btnListarTodos, btnNovoContrato);
        barraPesquisa.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtPesquisa, Priority.ALWAYS);

        HBox barrasFiltro = criarPainelFiltrosContratos();

        tabelaContratos = new TableView<>();
        listaContratos = FXCollections.observableArrayList();
        tabelaContratos.setItems(listaContratos);
        tabelaContratos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaContratos.setStyle("-fx-background-color: transparent;");
        tabelaContratos.setPlaceholder(criarEmptyStateContratos());

        configurarColunasContratos();

        tabelaContratos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) {
                        preencherFormularioContrato(novo);
                    }
                }
        );
        tabelaContratos.setRowFactory(tv -> {
            TableRow<Contrato> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Contrato contrato = row.getItem();
                    tabelaContratos.getSelectionModel().select(contrato);
                    abrirVisualizacaoContrato(contrato);
                }
            });
            return row;
        });

        VBox.setVgrow(tabelaContratos, Priority.ALWAYS);

        Label lblResumoTabela = new Label("Use filtros para destacar situacoes criticas e selecione uma linha para editar.");
        lblResumoTabela.setWrapText(true);
        lblResumoTabela.setStyle("-fx-text-fill: #6b7b8c; -fx-font-size: 11px;");

        VBox painel = new VBox(10, barraPesquisa, lblResumoTabela, barrasFiltro, tabelaContratos);
        painel.setPadding(new Insets(15));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(20,35,50,0.10), 14,0,0,4);");
        painel.setMaxWidth(Double.MAX_VALUE);

        painelTabelaStack = new StackPane(painel);
        overlayVisualizacaoContrato = criarOverlayVisualizacaoContrato(formContrato, painelClausulas);
        painelTabelaStack.getChildren().add(overlayVisualizacaoContrato);
        painelTabelaStack.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(painelTabelaStack, Priority.ALWAYS);
        return painelTabelaStack;
    }

    private void configurarColunasContratos() {
        TableColumn<Contrato, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(35);

        TableColumn<Contrato, String> colNumero = new TableColumn<>("Nº Contrato");
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroContrato"));
        colNumero.setPrefWidth(90);

        TableColumn<Contrato, String> colParceiro = new TableColumn<>("Parceiro");
        colParceiro.setCellValueFactory(new PropertyValueFactory<>("parceiroNome"));
        colParceiro.setPrefWidth(130);

        TableColumn<Contrato, String> colObjeto = new TableColumn<>("Objeto");
        colObjeto.setCellValueFactory(new PropertyValueFactory<>("objeto"));
        colObjeto.setPrefWidth(150);

        TableColumn<Contrato, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(80);

        TableColumn<Contrato, BigDecimal> colValor = new TableColumn<>("Valor (R$)");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorContrato"));
        colValor.setPrefWidth(90);
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("R$ %,.2f", item));
                }
            }
        });

        TableColumn<Contrato, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(95);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                    return;
                }

                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    setText("");
                    setGraphic(null);
                    return;
                }

                Contrato contrato = getTableView().getItems().get(index);
                Label badge = criarBadgeStatus(contrato);
                setText("");
                setGraphic(badge);
            }
        });

        TableColumn<Contrato, LocalDate> colVencimento = new TableColumn<>("Vencimento");
        colVencimento.setPrefWidth(85);
        colVencimento.setCellValueFactory(new PropertyValueFactory<>("dataFim"));
        colVencimento.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("—");
                    setStyle("");
                } else {
                    setText(item.format(FMT));
                    if (item.isBefore(LocalDate.now())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        java.util.List<TableColumn<Contrato, ?>> colunasContratos = new java.util.ArrayList<>();
        colunasContratos.add(colId);
        colunasContratos.add(colNumero);
        colunasContratos.add(colParceiro);
        colunasContratos.add(colObjeto);
        colunasContratos.add(colTipo);
        colunasContratos.add(colValor);
        colunasContratos.add(colStatus);
        colunasContratos.add(colVencimento);
        tabelaContratos.getColumns().addAll(colunasContratos);
    }

    private StackPane criarOverlayVisualizacaoContrato(VBox formContrato, VBox painelClausulas) {
        lblVisualizacaoTitulo = new Label("Novo contrato");
        lblVisualizacaoTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblVisualizacaoTitulo.setStyle("-fx-text-fill: #183b56;");

        lblVisualizacaoResumo = new Label("Use este painel ampliado para consultar, criar, editar e excluir contratos e clausulas.");
        lblVisualizacaoResumo.setWrapText(true);
        lblVisualizacaoResumo.setStyle("-fx-text-fill: #5f6c7b; -fx-font-size: 12px;");

        Button btnEditar = new Button("Novo contrato");
        btnEditar.setStyle(estiloBotaoPrimario());
        btnEditar.setOnAction(e -> abrirVisualizacaoContrato(null));

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle(estiloBotaoSecundario());
        btnFechar.setOnAction(e -> fecharVisualizacaoContrato());

        HBox acoes = new HBox(10, btnEditar, btnFechar);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane scrollForm = new ScrollPane(formContrato);
        scrollForm.setFitToWidth(true);
        scrollForm.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollForm.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollClausulas = new ScrollPane(painelClausulas);
        scrollClausulas.setFitToWidth(true);
        scrollClausulas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollClausulas.setStyle("-fx-background-color: transparent;");

        HBox conteudo = new HBox(18, scrollForm, scrollClausulas);
        HBox.setHgrow(scrollForm, Priority.ALWAYS);
        HBox.setHgrow(scrollClausulas, Priority.ALWAYS);
        scrollForm.prefWidthProperty().bind(conteudo.widthProperty().multiply(0.46));
        scrollClausulas.prefWidthProperty().bind(conteudo.widthProperty().multiply(0.54));

        VBox card = new VBox(16, lblVisualizacaoTitulo, lblVisualizacaoResumo, conteudo, acoes);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 22;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 22;"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.24), 28,0,0,10);");

        overlayVisualizacaoContrato = new StackPane(card);
        overlayVisualizacaoContrato.setPadding(new Insets(24));
        overlayVisualizacaoContrato.setStyle("-fx-background-color: rgba(15, 23, 42, 0.24);");
        overlayVisualizacaoContrato.setVisible(false);
        overlayVisualizacaoContrato.setManaged(false);
        card.prefWidthProperty().bind(painelTabelaStack.widthProperty().multiply(0.9));
        card.prefHeightProperty().bind(painelTabelaStack.heightProperty().multiply(0.9));
        return overlayVisualizacaoContrato;
    }

    private void abrirVisualizacaoContrato(Contrato contrato) {
        if (overlayVisualizacaoContrato == null) {
            return;
        }
        if (contrato != null) {
            preencherFormularioContrato(contrato);
        } else {
            limparFormularioContrato();
        }
        atualizarVisualizacaoContrato();
        overlayVisualizacaoContrato.setManaged(true);
        overlayVisualizacaoContrato.setVisible(true);
    }

    private void fecharVisualizacaoContrato() {
        if (overlayVisualizacaoContrato == null) {
            return;
        }
        overlayVisualizacaoContrato.setVisible(false);
        overlayVisualizacaoContrato.setManaged(false);
    }

    private void atualizarVisualizacaoContrato() {
        if (lblVisualizacaoTitulo == null || lblVisualizacaoResumo == null) {
            return;
        }

        if (contratoSelecionado == null) {
            lblVisualizacaoTitulo.setText("Novo contrato");
            lblVisualizacaoResumo.setText("Preencha os dados principais e depois cadastre as clausulas vinculadas.");
            return;
        }

        lblVisualizacaoTitulo.setText("Contrato " + textoOuPadrao(contratoSelecionado.getNumeroContrato(), "sem numero"));
        lblVisualizacaoResumo.setText(textoOuPadrao(contratoSelecionado.getParceiroNome(), "Parceiro nao informado")
                + " � " + textoOuPadrao(contratoSelecionado.getStatus(), "Status nao informado")
                + " � vigencia " + formatarData(contratoSelecionado.getDataInicio()) + " ate "
                + formatarData(contratoSelecionado.getDataFim()));
    }

    private VBox criarCartaoDetalhe(String titulo, String valor) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTitulo.setStyle("-fx-text-fill: #1f4e79;");

        Label lblValor = new Label(valor);
        lblValor.setWrapText(true);
        lblValor.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px;");

        VBox card = new VBox(4, lblTitulo, lblValor);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14;");
        return card;
    }

    // ===================================================================
    // CRUD CONTRATOS
    // ===================================================================

    private void salvarContrato() {
        // --- Coleta campos ---
        String numeroContrato = txtNumeroContrato.getText().trim();
        Parceiro parceiroSel = cbParceiro.getValue();
        String objeto = txtObjeto.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String tipo = cbTipo.getValue();
        String valorStr = txtValor.getText().trim();
        String multaStr = txtMulta.getText().trim();
        String formaPag = cbFormaPagamento.getValue();
        LocalDate dataInicio = txtDataInicio.getValue();
        LocalDate dataFim = txtDataFim.getValue();
        String status = cbStatus.getValue();
        String observacoes = txtObservacoes.getText().trim();

        // --- Validações ---
        if (numeroContrato.isEmpty()) {
            AlertaUtil.aviso("Campo obrigatório", "Informe o número do contrato.");
            txtNumeroContrato.requestFocus();
            return;
        }
        if (parceiroSel == null) {
            AlertaUtil.aviso("Campo obrigatório", "Selecione o parceiro.");
            cbParceiro.requestFocus();
            return;
        }
        if (objeto.isEmpty()) {
            AlertaUtil.aviso("Campo obrigatório", "Informe o objeto do contrato.");
            txtObjeto.requestFocus();
            return;
        }
        if (tipo == null) {
            AlertaUtil.aviso("Campo obrigatório", "Selecione o tipo.");
            return;
        }
        if (formaPag == null) {
            AlertaUtil.aviso("Campo obrigatório", "Selecione a forma de pagamento.");
            return;
        }
        if (status == null) {
            AlertaUtil.aviso("Campo obrigatório", "Selecione o status.");
            return;
        }

        if (dataInicio == null) {
            AlertaUtil.aviso("Campo obrigatório", "Informe a data de início.");
            txtDataInicio.requestFocus();
            return;
        }
        if (dataFim == null) {
            AlertaUtil.aviso("Campo obrigatório", "Informe a data de fim.");
            txtDataFim.requestFocus();
            return;
        }

        // Valor (BigDecimal)
        BigDecimal valor = BigDecimal.ZERO;
        if (!valorStr.isEmpty()) {
            try {
                valor = new BigDecimal(valorStr.replace(",", "."));
            } catch (NumberFormatException e) {
                AlertaUtil.aviso("Valor inválido", "O valor deve ser numérico.");
                txtValor.requestFocus();
                return;
            }
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            AlertaUtil.aviso("Valor inválido", "O valor não pode ser negativo.");
            txtValor.requestFocus();
            return;
        }

        // Multa (BigDecimal)
        BigDecimal multa = BigDecimal.ZERO;
        if (!multaStr.isEmpty()) {
            try {
                multa = new BigDecimal(multaStr.replace(",", "."));
            } catch (NumberFormatException e) {
                AlertaUtil.aviso("Multa inválida", "A multa deve ser numérica.");
                txtMulta.requestFocus();
                return;
            }
        }

        if (multa.compareTo(BigDecimal.ZERO) < 0) {
            AlertaUtil.aviso("Multa inválida", "A multa não pode ser negativa.");
            txtMulta.requestFocus();
            return;
        }

        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            AlertaUtil.aviso("Data inválida", "Data fim não pode ser anterior à data início.");
            return;
        }

        boolean isInsercao = (contratoSelecionado == null);
        int idAtual = isInsercao ? 0 : contratoSelecionado.getId();
        if (contratoDAO.numeroJaExiste(numeroContrato, idAtual)) {
            AlertaUtil.erro("Numero duplicado",
                    "Ja existe um contrato cadastrado com este numero.");
            txtNumeroContrato.requestFocus();
            return;
        }

        // --- Monta o objeto ---
        Contrato contrato = new Contrato();
        contrato.setNumeroContrato(numeroContrato);
        contrato.setParceiroId(parceiroSel.getId());
        contrato.setObjeto(objeto);
        contrato.setDescricao(descricao);
        contrato.setTipo(tipo);
        contrato.setValorContrato(valor);
        contrato.setMulta(multa);
        contrato.setFormaPagamento(formaPag);
        contrato.setDataInicio(dataInicio);
        contrato.setDataFim(dataFim);
        contrato.setStatus(status);
        contrato.setObservacoes(observacoes);

        // --- Persiste ---
        boolean sucesso;

        if (isInsercao) {
            sucesso = contratoDAO.inserir(contrato);
        } else {
            contrato.setId(contratoSelecionado.getId());
            sucesso = contratoDAO.atualizar(contrato);
        }

        if (sucesso) {
            AlertaUtil.info("Sucesso",
                    isInsercao ? "Contrato cadastrado com sucesso."
                            : "Contrato atualizado com sucesso.");
            fecharVisualizacaoContrato();
            limparFormularioContrato();
            atualizarTabelaContratos();
        } else {
            AlertaUtil.erro("Erro", "Não foi possível salvar o contrato.");
        }
    }

    private void excluirContrato() {
        if (contratoSelecionado == null) {
            AlertaUtil.aviso("Nenhum contrato selecionado",
                    "Selecione um contrato na tabela para excluir.");
            return;
        }

        boolean confirma = AlertaUtil.confirmar("Confirmar Exclusão",
                "Deseja excluir o contrato " + contratoSelecionado.getNumeroContrato()
                        + "?\n\nTodas as cláusulas vinculadas também serão removidas.");

        if (confirma) {
            boolean sucesso = contratoDAO.excluir(contratoSelecionado.getId());
            if (sucesso) {
                AlertaUtil.info("Sucesso", "Contrato excluído.");
                fecharVisualizacaoContrato();
                limparFormularioContrato();
                atualizarTabelaContratos();
            } else {
                AlertaUtil.erro("Erro", "Não foi possível excluir o contrato.");
            }
        }
    }

    private void pesquisarContratos() {
        fecharVisualizacaoContrato();
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            atualizarTabelaContratos();
            return;
        }
        listaContratos.setAll(contratoDAO.pesquisar(termo));
    }

    private void atualizarTabelaContratos() {
        aplicarFiltroContratos("TODOS");
    }

    private HBox criarPainelFiltrosContratos() {
        btnFiltroTodos = criarBotaoFiltro("Listar todos", "TODOS");
        btnFiltroAtivos = criarBotaoFiltro("Ativos", "ATIVO");
        btnFiltroConcluidos = criarBotaoFiltro("Concluídos", "CONCLUIDO");
        btnFiltroSuspensos = criarBotaoFiltro("Suspensos", "SUSPENSO");
        btnFiltroCancelados = criarBotaoFiltro("Cancelados", "CANCELADO");
        btnFiltroVencidos = criarBotaoFiltro("Vencidos", "VENCIDOS");
        btnFiltroAVencer = criarBotaoFiltro("A vencer", "AVENCER");

        HBox filtros = new HBox(8,
                btnFiltroTodos,
                btnFiltroAtivos,
                btnFiltroConcluidos,
                btnFiltroSuspensos,
                btnFiltroCancelados,
                btnFiltroVencidos,
                btnFiltroAVencer
        );
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(new Insets(4, 0, 0, 0));
        return filtros;
    }

    private Button criarBotaoFiltro(String texto, String filtro) {
        Button botao = new Button(texto);
        botao.setPrefHeight(32);
        botao.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #34495e; -fx-cursor: hand;"
                + "-fx-background-radius: 999; -fx-border-color: #d7e0e8; -fx-border-radius: 999;"
                + "-fx-font-weight: bold;");
        botao.setOnAction(e -> aplicarFiltroContratos(filtro));
        return botao;
    }

    private void aplicarFiltroContratos(String filtro) {
        fecharVisualizacaoContrato();
        filtroAtual = filtro;
        switch (filtro) {
            case "ATIVO" -> listaContratos.setAll(contratoDAO.listarPorStatus("ATIVO"));
            case "CONCLUIDO" -> listaContratos.setAll(contratoDAO.listarPorStatus("CONCLUIDO"));
            case "CANCELADO" -> listaContratos.setAll(contratoDAO.listarPorStatus("CANCELADO"));
            case "SUSPENSO" -> listaContratos.setAll(contratoDAO.listarPorStatus("SUSPENSO"));
            case "VENCIDOS" -> listaContratos.setAll(contratoDAO.listarContratosVencidos());
            case "AVENCER" -> listaContratos.setAll(contratoDAO.listarContratosAVencer());
            default -> listaContratos.setAll(contratoDAO.listarTodos());
        }
        txtPesquisa.clear();
        atualizarEstiloFiltros();
    }

    private void atualizarEstiloFiltros() {
        Button[] botoes = {
                btnFiltroTodos,
                btnFiltroAtivos,
                btnFiltroConcluidos,
                btnFiltroSuspensos,
                btnFiltroCancelados,
                btnFiltroVencidos,
                btnFiltroAVencer
        };
        for (Button botao : botoes) {
            if (botao == null) {
                continue;
            }
            if (botao.getText().equalsIgnoreCase("Listar todos") && "TODOS".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("Ativos") && "ATIVO".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("Concluídos") && "CONCLUIDO".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("Suspensos") && "SUSPENSO".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("Cancelados") && "CANCELADO".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("Vencidos") && "VENCIDOS".equals(filtroAtual)
                    || botao.getText().equalsIgnoreCase("A vencer") && "AVENCER".equals(filtroAtual)) {
                botao.setStyle("-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);"
                        + "-fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 999;"
                        + "-fx-border-radius: 999; -fx-font-weight: bold;");
            } else {
                botao.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #34495e; -fx-cursor: hand;"
                        + "-fx-background-radius: 999; -fx-border-color: #d7e0e8; -fx-border-radius: 999;"
                        + "-fx-font-weight: bold;");
            }
        }
    }

    // ===================================================================
    // CRUD CLÁUSULAS
    // ===================================================================

    private void salvarClausula() {
        if (contratoSelecionado == null) {
            AlertaUtil.aviso("Contrato necessário",
                    "Selecione um contrato antes de gerenciar cláusulas.");
            return;
        }

        String numStr = txtClausulaNumero.getText().trim();
        String descricao = txtClausulaDescricao.getText().trim();

        if (numStr.isEmpty()) {
            AlertaUtil.aviso("Campo obrigatório", "Informe o número da cláusula.");
            txtClausulaNumero.requestFocus();
            return;
        }

        int numero;
        try {
            numero = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            AlertaUtil.aviso("Número inválido", "O número da cláusula deve ser inteiro.");
            txtClausulaNumero.requestFocus();
            return;
        }

        if (numero <= 0) {
            AlertaUtil.aviso("Número inválido", "O número deve ser maior que zero.");
            txtClausulaNumero.requestFocus();
            return;
        }

        if (descricao.isEmpty()) {
            AlertaUtil.aviso("Campo obrigatório", "Informe a descrição da cláusula.");
            txtClausulaDescricao.requestFocus();
            return;
        }

        // Verifica duplicidade
        int idIgnorar = (clausulaSelecionada == null) ? 0 : clausulaSelecionada.getId();
        if (clausulaDAO.numeroDuplicado(contratoSelecionado.getId(), numero, idIgnorar)) {
            AlertaUtil.erro("Número duplicado",
                    "Já existe a cláusula nº " + numero + " neste contrato.");
            txtClausulaNumero.requestFocus();
            return;
        }

        Clausula clausula = new Clausula();
        clausula.setContratoId(contratoSelecionado.getId());
        clausula.setNumero(numero);
        clausula.setDescricao(descricao);

        boolean isInsercao = (clausulaSelecionada == null);
        boolean sucesso;

        if (isInsercao) {
            sucesso = clausulaDAO.inserir(clausula);
        } else {
            clausula.setId(clausulaSelecionada.getId());
            sucesso = clausulaDAO.atualizar(clausula);
        }

        if (sucesso) {
            AlertaUtil.info("Sucesso",
                    isInsercao ? "Cláusula adicionada." : "Cláusula atualizada.");
            limparFormularioClausula();
            atualizarTabelaClausulas();
            if (overlayVisualizacaoContrato != null && overlayVisualizacaoContrato.isVisible() && contratoSelecionado != null) {
                atualizarVisualizacaoContrato();
            }
        } else {
            AlertaUtil.erro("Erro", "Não foi possível salvar a cláusula.");
        }
    }

    private void excluirClausula() {
        if (clausulaSelecionada == null) {
            AlertaUtil.aviso("Nenhuma cláusula selecionada",
                    "Selecione uma cláusula na tabela abaixo.");
            return;
        }

        boolean confirma = AlertaUtil.confirmar("Confirmar Exclusão",
                "Excluir a Cláusula nº " + clausulaSelecionada.getNumero() + "?");

        if (confirma) {
            boolean sucesso = clausulaDAO.excluir(clausulaSelecionada.getId());
            if (sucesso) {
                AlertaUtil.info("Sucesso", "Cláusula excluída.");
                limparFormularioClausula();
                atualizarTabelaClausulas();
                if (overlayVisualizacaoContrato != null && overlayVisualizacaoContrato.isVisible() && contratoSelecionado != null) {
                    atualizarVisualizacaoContrato();
                }
            } else {
                AlertaUtil.erro("Erro", "Não foi possível excluir a cláusula.");
            }
        }
    }

    private void atualizarTabelaClausulas() {
        if (contratoSelecionado != null) {
            listaClausulas.setAll(clausulaDAO.listarPorContrato(contratoSelecionado.getId()));
        } else {
            listaClausulas.clear();
        }
        atualizarResumoClausulas();
    }

    private void configurarChecklistContrato() {
        txtNumeroContrato.textProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        cbParceiro.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        txtObjeto.textProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        cbTipo.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        cbFormaPagamento.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        txtDataInicio.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        txtDataFim.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        cbStatus.valueProperty().addListener((obs, antigo, novo) -> atualizarChecklistContrato());
        atualizarChecklistContrato();
    }

    private void atualizarChecklistContrato() {
        if (lblChecklistContrato == null) {
            return;
        }

        List<String> pendencias = new ArrayList<>();
        if (txtNumeroContrato == null || txtNumeroContrato.getText().trim().isEmpty()) {
            pendencias.add("numero");
        }
        if (cbParceiro == null || cbParceiro.getValue() == null) {
            pendencias.add("parceiro");
        }
        if (txtObjeto == null || txtObjeto.getText().trim().isEmpty()) {
            pendencias.add("objeto");
        }
        if (cbTipo == null || cbTipo.getValue() == null) {
            pendencias.add("tipo");
        }
        if (cbFormaPagamento == null || cbFormaPagamento.getValue() == null) {
            pendencias.add("forma de pagamento");
        }
        if (txtDataInicio == null || txtDataInicio.getValue() == null) {
            pendencias.add("data de inicio");
        }
        if (txtDataFim == null || txtDataFim.getValue() == null) {
            pendencias.add("data de fim");
        }
        if (cbStatus == null || cbStatus.getValue() == null) {
            pendencias.add("status");
        }

        boolean vigenciaInvalida = txtDataInicio != null && txtDataFim != null
                && txtDataInicio.getValue() != null && txtDataFim.getValue() != null
                && txtDataFim.getValue().isBefore(txtDataInicio.getValue());

        if (vigenciaInvalida) {
            lblChecklistContrato.setText("Revise a vigencia: a data fim nao pode ser anterior a data de inicio.");
            lblChecklistContrato.setStyle("-fx-background-color: #fff1f2; -fx-background-radius: 12;"
                    + "-fx-border-color: #fecdd3; -fx-border-radius: 12;"
                    + "-fx-padding: 10 12; -fx-text-fill: #b42318; -fx-font-size: 11px; -fx-font-weight: bold;");
            return;
        }

        if (pendencias.isEmpty()) {
            lblChecklistContrato.setText("Formulario pronto para salvar. Revise valores e observacoes opcionais antes de confirmar.");
            lblChecklistContrato.setStyle("-fx-background-color: #ecfdf3; -fx-background-radius: 12;"
                    + "-fx-border-color: #abefc6; -fx-border-radius: 12;"
                    + "-fx-padding: 10 12; -fx-text-fill: #027a48; -fx-font-size: 11px; -fx-font-weight: bold;");
            return;
        }

        lblChecklistContrato.setText("Pendencias do cadastro: faltam " + String.join(", ", pendencias) + ".");
        lblChecklistContrato.setStyle("-fx-background-color: #fffaeb; -fx-background-radius: 12;"
                + "-fx-border-color: #fedf89; -fx-border-radius: 12;"
                + "-fx-padding: 10 12; -fx-text-fill: #b54708; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    // ===================================================================
    // PREENCHIMENTO / LIMPEZA
    // ===================================================================

    private void preencherFormularioContrato(Contrato c) {
        contratoSelecionado = c;

        txtNumeroContrato.setText(c.getNumeroContrato() != null ? c.getNumeroContrato() : "");
        txtObjeto.setText(c.getObjeto() != null ? c.getObjeto() : "");
        txtDescricao.setText(c.getDescricao() != null ? c.getDescricao() : "");
        cbTipo.setValue(c.getTipo());

        // BigDecimal → String para exibição no campo texto
        txtValor.setText(c.getValorContrato() != null
                && c.getValorContrato().compareTo(BigDecimal.ZERO) > 0
                ? c.getValorContrato().toPlainString() : "");

        txtMulta.setText(c.getMulta() != null
                && c.getMulta().compareTo(BigDecimal.ZERO) > 0
                ? c.getMulta().toPlainString() : "");

        cbFormaPagamento.setValue(c.getFormaPagamento());
        txtDataInicio.setValue(c.getDataInicio());
        txtDataFim.setValue(c.getDataFim());
        cbStatus.setValue(c.getStatus());
        txtObservacoes.setText(c.getObservacoes() != null ? c.getObservacoes() : "");

        // Seleciona o parceiro correto no ComboBox
        for (Parceiro p : cbParceiro.getItems()) {
            if (p.getId() == c.getParceiroId()) {
                cbParceiro.setValue(p);
                break;
            }
        }

        // Atualiza cláusulas do contrato selecionado
        atualizarResumoContrato();
        atualizarEstadoClausulas();
        atualizarTabelaClausulas();
        limparFormularioClausula();

        // Sugere próximo número de cláusula
        int proximo = clausulaDAO.proximoNumero(c.getId());
        txtClausulaNumero.setText(String.valueOf(proximo));
        atualizarChecklistContrato();
    }

    private void limparFormularioContrato() {
        contratoSelecionado = null;
        clausulaSelecionada = null;

        txtNumeroContrato.clear();
        cbParceiro.setValue(null);
        txtObjeto.clear();
        txtDescricao.clear();
        cbTipo.setValue(null);
        txtValor.clear();
        txtMulta.clear();
        cbFormaPagamento.setValue(null);
        txtDataInicio.setValue(null);
        txtDataFim.setValue(null);
        cbStatus.setValue(null);
        txtObservacoes.clear();

        tabelaContratos.getSelectionModel().clearSelection();

        // Limpa cláusulas
        limparFormularioClausula();
        listaClausulas.clear();
        atualizarResumoContrato();
        atualizarEstadoClausulas();
        atualizarResumoClausulas();
        atualizarChecklistContrato();
        fecharVisualizacaoContrato();
    }

    private void preencherFormularioClausula(Clausula c) {
        clausulaSelecionada = c;
        txtClausulaNumero.setText(String.valueOf(c.getNumero()));
        txtClausulaDescricao.setText(c.getDescricao() != null ? c.getDescricao() : "");
    }

    private void limparFormularioClausula() {
        clausulaSelecionada = null;
        txtClausulaNumero.clear();
        txtClausulaDescricao.clear();
        tabelaClausulas.getSelectionModel().clearSelection();

        // Sugere próximo número se tem contrato selecionado
        if (contratoSelecionado != null) {
            int proximo = clausulaDAO.proximoNumero(contratoSelecionado.getId());
            txtClausulaNumero.setText(String.valueOf(proximo));
        }
    }

    /**
     * Habilita/desabilita o painel de cláusulas conforme haja contrato selecionado.
     * Quando nenhum contrato está selecionado, o painel fica opaco e desabilitado,
     * indicando visualmente que o usuário precisa selecionar um contrato primeiro.
     */
    private void atualizarEstadoClausulas() {
        boolean desabilitado = (contratoSelecionado == null);
        painelClausulas.setDisable(desabilitado);
        painelClausulas.setOpacity(desabilitado ? 0.5 : 1.0);
        atualizarResumoClausulas();
    }

    // ===================================================================
    // UTILITÁRIOS
    // ===================================================================

    private Label criarResumoPainel(String titulo, String texto) {
        Label label = new Label(titulo + "\n" + texto);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 12;"
                + "-fx-padding: 10 12; -fx-text-fill: #516170; -fx-font-size: 11px;");
        return label;
    }

    private void atualizarResumoContrato() {
        if (lblResumoContrato == null) {
            return;
        }

        if (contratoSelecionado == null) {
            lblResumoContrato.setText("Novo contrato\n"
                    + "Preencha os campos principais e selecione um registro na tabela quando quiser editar.");
            return;
        }

        String parceiro = cbParceiro.getValue() != null
                ? cbParceiro.getValue().getRazaoSocial() : "Parceiro nao informado";
        String status = cbStatus.getValue() != null ? cbStatus.getValue() : "Status em definicao";
        String vigencia = txtDataFim.getValue() != null ? txtDataFim.getValue().format(FMT) : "sem data final";

        lblResumoContrato.setText("Contrato em edicao\n"
                + contratoSelecionado.getNumeroContrato() + " • " + parceiro
                + " • " + status + " • vigencia ate " + vigencia + ".");
    }

    private void atualizarResumoClausulas() {
        if (lblResumoClausulas == null || tabelaClausulas == null) {
            return;
        }

        if (contratoSelecionado == null) {
            lblResumoClausulas.setText("Clausulas bloqueadas\n"
                    + "Selecione um contrato na tabela para cadastrar e revisar as clausulas vinculadas.");
            tabelaClausulas.setPlaceholder(criarResumoPainel(
                    "Selecione um contrato",
                    "As clausulas ficam disponiveis depois que um contrato e carregado para edicao."
            ));
            return;
        }

        int quantidade = listaClausulas != null ? listaClausulas.size() : 0;
        String numeroContrato = contratoSelecionado.getNumeroContrato() != null
                ? contratoSelecionado.getNumeroContrato() : "sem numero";

        if (quantidade == 0) {
            lblResumoClausulas.setText("Contrato pronto para detalhamento\n"
                    + "O contrato " + numeroContrato + " ainda nao possui clausulas cadastradas.");
            tabelaClausulas.setPlaceholder(criarResumoPainel(
                    "Nenhuma clausula cadastrada",
                    "Use o formulario acima para incluir a primeira clausula deste contrato."
            ));
            return;
        }

        lblResumoClausulas.setText("Clausulas em revisao\n"
                + "O contrato " + numeroContrato + " possui " + quantidade
                + (quantidade == 1 ? " clausula cadastrada." : " clausulas cadastradas."));
        tabelaClausulas.setPlaceholder(criarResumoPainel(
                "Lista atualizada",
                "Nenhum item para exibir no momento."
        ));
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(FMT) : "nao informada";
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "Nao informado";
    }

    private String formatarFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.isBlank()) {
            return "Nao informado";
        }
        return formaPagamento.replace("_", " ").toLowerCase();
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private void estilizarCampo(Control control) {
        control.setStyle(
                "-fx-background-color: #f8fafc;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;"
                + "-fx-prompt-text-fill: #94a3b8;"
        );
    }

    private Label criarTextoApoio(String texto) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #6b7b8c; -fx-font-size: 11px;");
        return label;
    }

    private String estiloBotaoPrimario() {
        return "-fx-background-color: linear-gradient(to right, #4ca1af, #2c3e50);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"
                + "-fx-background-radius: 10; -fx-padding: 10 14;";
    }

    private String estiloBotaoSecundario() {
        return "-fx-background-color: #f8fafc; -fx-text-fill: #2c3e50;"
                + "-fx-border-color: #d7e0e8; -fx-border-radius: 10;"
                + "-fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand;";
    }

    private String estiloBotaoDestrutivo() {
        return "-fx-background-color: #fdecec; -fx-text-fill: #c0392b;"
                + "-fx-border-color: #f4c7c3; -fx-border-radius: 10;"
                + "-fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand;";
    }

    private void carregarParceiros() {
        List<Parceiro> parceiros = parceiroDAO.listarTodos();
        cbParceiro.setItems(FXCollections.observableArrayList(parceiros));

        cbParceiro.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Parceiro item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getRazaoSocial());
            }
        });

        cbParceiro.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Parceiro item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getRazaoSocial());
            }
        });
    }

    private DatePicker criarDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(FMT);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(value.trim(), FMT);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        return datePicker;
    }

    private void aplicarMascaraMonetaria(TextField campo) {
        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.isEmpty() || novoTexto.matches("\\d{0,12}([,.]\\d{0,2})?")) {
                return change;
            }
            return null;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    private Label criarTituloSecao(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #4ca1af; -fx-padding: 8 0 0 0;");
        return label;
    }

    private VBox criarEmptyStateContratos() {
        Label titulo = new Label("Nenhum contrato encontrado");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titulo.setStyle("-fx-text-fill: #2c3e50;");

        Label detalhe = new Label("Cadastre um contrato ou ajuste a pesquisa.");
        detalhe.setStyle("-fx-text-fill: #7f8c8d;");

        VBox box = new VBox(6, titulo, detalhe);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        return box;
    }

    private Label criarBadgeStatus(Contrato contrato) {
        String texto = contrato.getStatus();
        String corFundo = "#eceff1";
        String corTexto = "#37474f";

        LocalDate vencimento = contrato.getDataFim();
        boolean ativo = "ATIVO".equalsIgnoreCase(contrato.getStatus());
        if (ativo && vencimento != null && vencimento.isBefore(LocalDate.now())) {
            texto = "VENCIDO";
            corFundo = "#ffebee";
            corTexto = "#b71c1c";
        } else if (ativo && vencimento != null && !vencimento.isAfter(LocalDate.now().plusDays(30))) {
            texto = "A VENCER";
            corFundo = "#fff8e1";
            corTexto = "#8a5a00";
        } else if ("ATIVO".equalsIgnoreCase(contrato.getStatus())) {
            corFundo = "#e8f5e9";
            corTexto = "#1b5e20";
        } else if ("CONCLUIDO".equalsIgnoreCase(contrato.getStatus())) {
            corFundo = "#e3f2fd";
            corTexto = "#0d47a1";
        } else if ("SUSPENSO".equalsIgnoreCase(contrato.getStatus())) {
            corFundo = "#fff8e1";
            corTexto = "#8a5a00";
        } else if ("CANCELADO".equalsIgnoreCase(contrato.getStatus())) {
            corFundo = "#eeeeee";
            corTexto = "#616161";
        }

        Label badge = new Label(texto);
        badge.setMinWidth(72);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: " + corFundo + ";"
                + "-fx-text-fill: " + corTexto + ";"
                + "-fx-font-size: 11px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 3 8;"
                + "-fx-background-radius: 999;");
        return badge;
    }
}

