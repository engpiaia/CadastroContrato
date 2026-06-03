package com.contratech.cadastrocontrato.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.contratech.cadastrocontrato.dao.ClausulaDAO;
import com.contratech.cadastrocontrato.dao.ContratoDAO;
import com.contratech.cadastrocontrato.dao.ParceiroDAO;
import com.contratech.cadastrocontrato.model.Clausula;
import com.contratech.cadastrocontrato.model.Contrato;
import com.contratech.cadastrocontrato.model.Parceiro;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AjudaUtil;
import com.contratech.cadastrocontrato.util.AlertaUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

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
    private TextField txtDataInicio;
    private TextField txtDataFim;
    private ComboBox<String> cbStatus;
    private TextArea txtObservacoes;
    private TextField txtPesquisa;

    // ==================== TABELA DE CONTRATOS ====================
    private TableView<Contrato> tabelaContratos;
    private ObservableList<Contrato> listaContratos;

    // ==================== CAMPOS DAS CLÁUSULAS ====================
    private TextField txtClausulaNumero;
    private TextArea txtClausulaDescricao;
    private TableView<Clausula> tabelaClausulas;
    private ObservableList<Clausula> listaClausulas;
    private VBox painelClausulas;

    // ==================== CONTROLE DE ESTADO ====================
    private Contrato contratoSelecionado = null;
    private Clausula clausulaSelecionada = null;

    public TelaContrato(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {

    // ===== HEADER PADRÃO =====
    Label lblTitulo = new Label("Contratos  |  " + usuarioLogado.getNome());
    lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
    lblTitulo.setStyle("-fx-text-fill: white;");

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

    HBox header = new HBox(12, lblTitulo, lblPerfil, espacador, btnAjuda, btnMenu, btnLogout);
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(18));
    header.setStyle(
            "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
    );

    // ===== FORMULÁRIO =====
    VBox formContrato = criarFormularioContrato();

    ScrollPane scrollForm = new ScrollPane(formContrato);
    scrollForm.setFitToWidth(true);
    scrollForm.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollForm.setStyle("-fx-background-color: transparent;");

    // ===== CLÁUSULAS VISÍVEL (FIXO) =====
    painelClausulas = criarPainelClausulas();

    VBox esquerda = new VBox(10, scrollForm, painelClausulas);
    esquerda.setPrefWidth(380);
    VBox.setVgrow(scrollForm, Priority.ALWAYS);

    if (usuarioLogado.getTipoUsuario() == Usuario.TipoUsuario.VISUALIZADOR) {
        esquerda.setDisable(true);
        esquerda.setOpacity(0.98);
    }

    // ===== TABELA =====
    VBox tabela = criarPainelTabelaContratos();

    HBox centro = new HBox(20, esquerda, tabela);
    centro.setPadding(new Insets(20));
    HBox.setHgrow(tabela, Priority.ALWAYS);

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

    atualizarTabelaContratos();
    atualizarEstadoClausulas();
}


    // ===================================================================
    // FORMULÁRIO DO CONTRATO
    // ===================================================================

    private VBox criarFormularioContrato() {
        Label lblForm = new Label("Dados do Contrato");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // --- Nº Contrato ---
        txtNumeroContrato = new TextField();
        txtNumeroContrato.setPromptText("Ex: 2026/001");
        txtNumeroContrato.setMaxWidth(Double.MAX_VALUE);

        // --- Parceiro ---
        cbParceiro = new ComboBox<>();
        cbParceiro.setPromptText("Selecione...");
        cbParceiro.setMaxWidth(Double.MAX_VALUE);
        carregarParceiros();

        // --- Objeto ---
        txtObjeto = new TextField();
        txtObjeto.setPromptText("Objeto do contrato");
        txtObjeto.setMaxWidth(Double.MAX_VALUE);

        // --- Descrição ---
        txtDescricao = new TextArea();
        txtDescricao.setPromptText("Descrição detalhada...");
        txtDescricao.setPrefRowCount(3);
        txtDescricao.setWrapText(true);
        txtDescricao.setMaxWidth(Double.MAX_VALUE);

        // --- Tipo ---
        cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "SERVICO", "FORNECIMENTO", "MISTO", "LOCACAO", "CONSULTORIA"));
        cbTipo.setPromptText("Tipo");
        cbTipo.setMaxWidth(Double.MAX_VALUE);

        // --- Valor e Multa ---
        txtValor = new TextField();
        txtValor.setPromptText("0.00");
        txtValor.setMaxWidth(Double.MAX_VALUE);

        txtMulta = new TextField();
        txtMulta.setPromptText("0.00");
        txtMulta.setMaxWidth(Double.MAX_VALUE);

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

        // --- Datas ---
        txtDataInicio = new TextField();
        txtDataInicio.setPromptText("dd/MM/yyyy");
        txtDataInicio.setMaxWidth(Double.MAX_VALUE);

        txtDataFim = new TextField();
        txtDataFim.setPromptText("dd/MM/yyyy");
        txtDataFim.setMaxWidth(Double.MAX_VALUE);

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

        // --- Observações ---
        txtObservacoes = new TextArea();
        txtObservacoes.setPromptText("Observações opcionais...");
        txtObservacoes.setPrefRowCount(2);
        txtObservacoes.setWrapText(true);
        txtObservacoes.setMaxWidth(Double.MAX_VALUE);

        // === Botões ===
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setPrefWidth(130);
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnSalvar.setOnAction(e -> salvarContrato());

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setPrefWidth(130);
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnExcluir.setOnAction(e -> excluirContrato());

        HBox botoesAcao = new HBox(10, btnSalvar, btnExcluir);

        Button btnLimpar = new Button("Novo Contrato");
        btnLimpar.setPrefWidth(270);
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setStyle("-fx-cursor: hand;");
        btnLimpar.setOnAction(e -> limparFormularioContrato());

        // === Monta ===
        VBox form = new VBox(8);
        form.setPadding(new Insets(15));
        form.setMinWidth(340);
        form.setMaxWidth(450);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        form.getChildren().addAll(
                lblForm,
                new Separator(),
                new Label("Nº Contrato:"), txtNumeroContrato,
                new Label("Parceiro:"), cbParceiro,
                new Label("Objeto:"), txtObjeto,
                new Label("Descrição:"), txtDescricao,
                new Label("Tipo:"), cbTipo,
                hbValores,
                new Label("Forma de Pagamento:"), cbFormaPagamento,
                hbDatas,
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

        // --- Nº da Cláusula ---
        txtClausulaNumero = new TextField();
        txtClausulaNumero.setPromptText("Nº");
        txtClausulaNumero.setPrefWidth(60);

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

        // --- Botões das cláusulas ---
        Button btnAddClausula = new Button("Salvar Cláusula");
        btnAddClausula.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        btnAddClausula.setMaxWidth(Double.MAX_VALUE);
        btnAddClausula.setOnAction(e -> salvarClausula());

        Button btnExcClausula = new Button("Excluir Cláusula");
        btnExcClausula.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-font-size: 11px;");
        btnExcClausula.setMaxWidth(Double.MAX_VALUE);
        btnExcClausula.setOnAction(e -> excluirClausula());

        Button btnLimparClausula = new Button("Limpar");
        btnLimparClausula.setStyle("-fx-cursor: hand; -fx-font-size: 11px;");
        btnLimparClausula.setMaxWidth(Double.MAX_VALUE);
        btnLimparClausula.setOnAction(e -> limparFormularioClausula());

        HBox botoesClausula = new HBox(5, btnAddClausula, btnExcClausula, btnLimparClausula);

        // --- Mini-tabela de cláusulas ---
        tabelaClausulas = new TableView<>();
        listaClausulas = FXCollections.observableArrayList();
        tabelaClausulas.setItems(listaClausulas);
        tabelaClausulas.setPrefHeight(180);
        VBox.setVgrow(tabelaClausulas, Priority.ALWAYS);
        tabelaClausulas.setPlaceholder(new Label("Nenhuma cláusula cadastrada."));

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
        painel.setStyle("-fx-background-color: #F3E5F5; -fx-background-radius: 6; "
                + "-fx-border-color: #CE93D8; -fx-border-radius: 6; -fx-border-width: 1;");
        painel.setMaxWidth(Double.MAX_VALUE);

        painel.getChildren().addAll(
                lblTitulo,
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

    private VBox criarPainelTabelaContratos() {
        txtPesquisa = new TextField();
        txtPesquisa.setPromptText("Pesquisar por nº contrato, objeto ou parceiro...");

        Button btnPesquisar = new Button("Pesquisar");
        btnPesquisar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnPesquisar.setOnAction(e -> pesquisarContratos());

        Button btnListarTodos = new Button("Listar Todos");
        btnListarTodos.setStyle("-fx-cursor: hand;");
        btnListarTodos.setOnAction(e -> atualizarTabelaContratos());

        txtPesquisa.setOnAction(e -> pesquisarContratos());

        HBox barraPesquisa = new HBox(10, txtPesquisa, btnPesquisar, btnListarTodos);
        barraPesquisa.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtPesquisa, Priority.ALWAYS);

        tabelaContratos = new TableView<>();
        listaContratos = FXCollections.observableArrayList();
        tabelaContratos.setItems(listaContratos);
        tabelaContratos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarColunasContratos();

        tabelaContratos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) {
                        preencherFormularioContrato(novo);
                    }
                }
        );

        VBox.setVgrow(tabelaContratos, Priority.ALWAYS);

        VBox painel = new VBox(10, barraPesquisa, tabelaContratos);
        painel.setPadding(new Insets(15));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 6;");
        painel.setMaxWidth(Double.MAX_VALUE);

        return painel;
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
        colStatus.setPrefWidth(70);

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
        String dataInicioStr = txtDataInicio.getText().trim();
        String dataFimStr = txtDataFim.getText().trim();
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

        // Datas
        LocalDate dataInicio = null;
        LocalDate dataFim = null;
        try {
            if (!dataInicioStr.isEmpty()) dataInicio = LocalDate.parse(dataInicioStr, FMT);
            if (!dataFimStr.isEmpty()) dataFim = LocalDate.parse(dataFimStr, FMT);
        } catch (DateTimeParseException e) {
            AlertaUtil.aviso("Data inválida", "Use o formato dd/MM/yyyy.");
            return;
        }

        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            AlertaUtil.aviso("Data inválida", "Data fim não pode ser anterior à data início.");
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
        boolean isInsercao = (contratoSelecionado == null);
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
                limparFormularioContrato();
                atualizarTabelaContratos();
            } else {
                AlertaUtil.erro("Erro", "Não foi possível excluir o contrato.");
            }
        }
    }

    private void pesquisarContratos() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            atualizarTabelaContratos();
            return;
        }
        listaContratos.setAll(contratoDAO.pesquisar(termo));
    }

    private void atualizarTabelaContratos() {
        listaContratos.setAll(contratoDAO.listarTodos());
        txtPesquisa.clear();
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
        txtDataInicio.setText(c.getDataInicio() != null ? c.getDataInicio().format(FMT) : "");
        txtDataFim.setText(c.getDataFim() != null ? c.getDataFim().format(FMT) : "");
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
        atualizarEstadoClausulas();
        atualizarTabelaClausulas();
        limparFormularioClausula();

        // Sugere próximo número de cláusula
        int proximo = clausulaDAO.proximoNumero(c.getId());
        txtClausulaNumero.setText(String.valueOf(proximo));
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
        txtDataInicio.clear();
        txtDataFim.clear();
        cbStatus.setValue(null);
        txtObservacoes.clear();

        tabelaContratos.getSelectionModel().clearSelection();

        // Limpa cláusulas
        limparFormularioClausula();
        listaClausulas.clear();
        atualizarEstadoClausulas();
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
    }

    // ===================================================================
    // UTILITÁRIOS
    // ===================================================================

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
}

