package com.contratech.contratos.view;

import com.contratech.contratos.dao.UsuarioDAO;
import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.model.Usuario.TipoUsuario;
import com.contratech.contratos.util.security.AuditUtil;
import com.contratech.contratos.util.security.SenhaUtil;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
 * Tela completa de CRUD de Usuários.
 * 
 * Acessível apenas por usuários ADMIN.
 * Estrutura: formulário à esquerda, tabela à direita.
 */
public class TelaUsuario {

    private final Stage stage;
    private final Usuario usuarioLogado;
    private final UsuarioDAO dao = new UsuarioDAO();

    // Componentes do formulário
    private TextField txtNome;
    private TextField txtSobrenome;
    private TextField txtEmail;
    private PasswordField txtSenha;
    private ComboBox<TipoUsuario> cmbTipo;
    private TextField txtPesquisa;

    // Tabela
    private TableView<Usuario> tabela;
    private ObservableList<Usuario> listaUsuarios;

    // Controle de estado: null = inserindo, preenchido = editando
    private Usuario usuarioSelecionado = null;

    public TelaUsuario(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {

    // Segurança
    if (usuarioLogado.getTipoUsuario() != Usuario.TipoUsuario.ADMIN) {
        AuditUtil.logDeniedAccess(usuarioLogado,
                "Abrir TelaUsuario (via URL/rotina)", "Permissão insuficiente");
        AlertaUtil.erro("Acesso negado", "Acesso restrito: somente ADMIN pode acessar Usuários.");
        new TelaPrincipal(stage, usuarioLogado).exibir();
        return;
    }

    // ===== HEADER PADRONIZADO =====
    Label lblTitulo = new Label("Usuários  |  " + usuarioLogado.getNome());
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

    Button btnVoltar = new Button("🏠 Menu");
    btnVoltar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 6 14;" +
            "-fx-font-weight: bold;"
    );
    btnVoltar.setOnAction(e -> new TelaPrincipal(stage, usuarioLogado).exibir());

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
            "Gestão de usuários",
            () -> new TelaUsuario(stage, usuarioLogado).exibir());

    Region espaco = new Region();
    HBox.setHgrow(espaco, Priority.ALWAYS);

    HBox barraSuperior = new HBox(12,
            lblTitulo,
            lblPerfil,
            espaco,
            btnAjuda,
            btnVoltar,
            btnLogout
    );

    barraSuperior.setAlignment(Pos.CENTER_LEFT);
    barraSuperior.setPadding(new Insets(18, 20, 18, 20));
    barraSuperior.setPrefHeight(78);
    barraSuperior.setMinHeight(78);
    barraSuperior.setMaxHeight(78);
    barraSuperior.setStyle(
            "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
    );

    // ===== FORMULÁRIO =====
    VBox formulario = criarFormulario();

    // ===== TABELA =====
    VBox painelTabela = criarPainelTabela();

    HBox centro = new HBox(18, formulario, painelTabela);
    centro.setPadding(new Insets(20));
    centro.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(painelTabela, Priority.ALWAYS);

    // ===== ROOT =====
    BorderPane root = new BorderPane();
    root.setTop(barraSuperior);
    root.setCenter(centro);
    root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef3f8, #dce6f1);");

    Scene scene = new Scene(root, 900, 550);
    AjudaUtil.registrarAtalhoF1(scene, stage, usuarioLogado,
            "Gestão de usuários",
            () -> new TelaUsuario(stage, usuarioLogado).exibir());

    stage.setTitle("Usuários - CadastroContrato");
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    atualizarTabela();
}


    /**
     * Constrói o painel de formulário com campos e botões de ação.
     */
    private VBox criarFormulario() {
        Label lblForm = new Label("Dados do Usuário");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtNome = new TextField();
        txtNome.setPromptText("Nome");
        txtNome.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtNome);

        txtSobrenome = new TextField();
        txtSobrenome.setPromptText("Sobrenome");
        txtSobrenome.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtSobrenome);

        txtEmail = new TextField();
        txtEmail.setPromptText("email@exemplo.com");
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtEmail);

        txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha (mín. 6 caracteres)");
        txtSenha.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtSenha);

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoUsuario.values()));
        cmbTipo.setPromptText("Tipo de Usuário");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(cmbTipo);

        // === Botões de ação ===
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setStyle(estiloBotaoPrimario());
        btnSalvar.setOnAction(e -> salvar());

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle(estiloBotaoDestrutivo());
        btnExcluir.setOnAction(e -> excluir());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setStyle(estiloBotaoSecundario());
        btnLimpar.setOnAction(e -> limparFormulario());

        HBox botoesAcao = new HBox(10, btnSalvar, btnExcluir);

        // === Monta o formulário ===
        VBox form = new VBox(8);
        form.setPadding(new Insets(15));
        form.setMinWidth(300);
        form.setMaxWidth(430);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(20,35,50,0.10), 14,0,0,4);");

        form.getChildren().addAll(
            lblForm,
            new Separator(),
            criarTituloSecao("Identificacao"),
            new Label("Nome:"), txtNome,
            new Label("Sobrenome:"), txtSobrenome,
            new Label("E-mail:"), txtEmail,
            new Label("Senha:"), txtSenha,
            criarTituloSecao("Acesso"),
            new Label("Tipo:"), cmbTipo,
            new Separator(),
            botoesAcao,
            btnLimpar
        );

        return form;
    }

    /**
     * Constrói o painel com campo de pesquisa e tabela de resultados.
     */
    private VBox criarPainelTabela() {
        // Campo de pesquisa
        txtPesquisa = new TextField();
        txtPesquisa.setPromptText("Pesquisar por nome...");
        txtPesquisa.setMaxWidth(Double.MAX_VALUE);
        estilizarCampo(txtPesquisa);

        Button btnPesquisar = new Button("Pesquisar");
        btnPesquisar.setStyle(estiloBotaoPrimario());
        btnPesquisar.setOnAction(e -> pesquisar());

        Button btnListarTodos = new Button("Listar Todos");
        btnListarTodos.setStyle(estiloBotaoSecundario());
        btnListarTodos.setOnAction(e -> atualizarTabela());

        // Enter no campo pesquisa
        txtPesquisa.setOnAction(e -> pesquisar());

        HBox barraPesquisa = new HBox(10, txtPesquisa, btnPesquisar, btnListarTodos);
        barraPesquisa.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtPesquisa, Priority.ALWAYS);

        // === Tabela ===
        tabela = new TableView<>();
        listaUsuarios = FXCollections.observableArrayList();
        tabela.setItems(listaUsuarios);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colunas
        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(40);

        TableColumn<Usuario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(120);

        TableColumn<Usuario, String> colSobrenome = new TableColumn<>("Sobrenome");
        colSobrenome.setCellValueFactory(new PropertyValueFactory<>("sobrenome"));
        colSobrenome.setPrefWidth(120);

        TableColumn<Usuario, String> colEmail = new TableColumn<>("E-mail");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);

        TableColumn<Usuario, TipoUsuario> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoUsuario"));
        colTipo.setPrefWidth(100);

        java.util.List<TableColumn<Usuario, ?>> colunas = new java.util.ArrayList<>();
        colunas.add(colId);
        colunas.add(colNome);
        colunas.add(colSobrenome);
        colunas.add(colEmail);
        colunas.add(colTipo);
        tabela.getColumns().addAll(colunas);

        // Ao clicar numa linha, preenche o formulário para edição
        tabela.getSelectionModel().selectedItemProperty().addListener(
            (obs, antigo, novo) -> {
                if (novo != null) {
                    preencherFormulario(novo);
                }
            }
        );

        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox painel = new VBox(10, barraPesquisa, tabela);
        painel.setPadding(new Insets(15));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-border-color: #dbe5ec; -fx-border-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(20,35,50,0.10), 14,0,0,4);");
        tabela.setStyle("-fx-background-color: transparent;");
        tabela.setPlaceholder(new Label("Nenhum usuario encontrado."));

        return painel;
    }

    // ===================================================================
    // OPERAÇÕES CRUD
    // ===================================================================

    /**
     * Salva (insere ou atualiza) o usuário com base no estado do formulário.
     * Se usuarioSelecionado == null → INSERT. Senão → UPDATE.
     */
    private void salvar() {
        // --- Validação ---
        String nome = txtNome.getText().trim();
        String sobrenome = txtSobrenome.getText().trim();
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText().trim();
        TipoUsuario tipo = cmbTipo.getValue();

        if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || tipo == null) {
            AlertaUtil.aviso("Campos obrigatórios",
                    "Preencha Nome, Sobrenome, E-mail e Tipo.");
            return;
        }

        // E-mail básico: contém @ e .
        if (!email.contains("@") || !email.contains(".")) {
            AlertaUtil.aviso("E-mail inválido", "Informe um e-mail válido.");
            return;
        }

        // Senha obrigatória somente na inserção
        boolean isInsercao = (usuarioSelecionado == null);
        if (isInsercao && (senha.isEmpty() || senha.length() < 6)) {
            AlertaUtil.aviso("Senha inválida",
                    "A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        // Verifica e-mail duplicado
        int idAtual = isInsercao ? 0 : usuarioSelecionado.getId();
        if (dao.emailJaExiste(email, idAtual)) {
            AlertaUtil.erro("E-mail duplicado",
                    "Já existe um usuário cadastrado com este e-mail.");
            return;
        }

        // --- Monta o objeto ---
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setSobrenome(sobrenome);
        usuario.setEmail(email);
        usuario.setTipoUsuario(tipo);

        // Só define senha se foi preenchida (edição pode manter a atual)
        if (!senha.isEmpty()) {
            usuario.setSenha(SenhaUtil.hashSHA256(senha));
        }

        // --- Persiste ---
        boolean sucesso;

        if (isInsercao) {
            sucesso = dao.inserir(usuario);
        } else {
            usuario.setId(usuarioSelecionado.getId());
            sucesso = dao.alterar(usuario);
        }

        if (sucesso) {
            AlertaUtil.info("Sucesso",
                    isInsercao ? "Usuário cadastrado com sucesso."
                               : "Usuário atualizado com sucesso.");
            limparFormulario();
            atualizarTabela();
        } else {
            AlertaUtil.erro("Erro", "Não foi possível salvar o usuário.");
        }
    }

    /**
     * Exclusão lógica do usuário selecionado.
     * Impede exclusão do próprio usuário logado.
     */
    private void excluir() {
        if (usuarioSelecionado == null) {
            AlertaUtil.aviso("Nenhum usuário selecionado",
                    "Selecione um usuário na tabela para excluir.");
            return;
        }

        // Proteção: não pode excluir a si mesmo
        if (usuarioSelecionado.getId() == usuarioLogado.getId()) {
            AlertaUtil.erro("Operação negada",
                    "Você não pode excluir seu próprio usuário.");
            return;
        }

        boolean confirma = AlertaUtil.confirmar("Confirmar Exclusão",
                "Deseja realmente excluir o usuário '"
                + usuarioSelecionado.getNome() + " " + usuarioSelecionado.getSobrenome()
                + "'?\n\nEsta ação pode ser revertida apenas via banco de dados.");

        if (confirma) {
            boolean sucesso = dao.excluir(usuarioSelecionado.getId());

            if (sucesso) {
                AlertaUtil.info("Sucesso", "Usuário excluído com sucesso.");
                limparFormulario();
                atualizarTabela();
            } else {
                AlertaUtil.erro("Erro", "Não foi possível excluir o usuário.");
            }
        }
    }

    /**
     * Pesquisa usuários por nome.
     */
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();

        if (termo.isEmpty()) {
            atualizarTabela();
            return;
        }

        listaUsuarios.setAll(dao.pesquisarPorNome(termo));
    }

    /**
     * Recarrega toda a tabela com os dados do banco.
     */
    private void atualizarTabela() {
        listaUsuarios.setAll(dao.listarTodos());
        txtPesquisa.clear();
    }

    /**
     * Preenche o formulário com os dados do usuário selecionado na tabela.
     * Ativa o modo "edição".
     */
    private void preencherFormulario(Usuario u) {
        usuarioSelecionado = u;
        txtNome.setText(u.getNome());
        txtSobrenome.setText(u.getSobrenome());
        txtEmail.setText(u.getEmail());
        txtSenha.clear(); // Nunca exibe a senha — campo fica vazio na edição
        cmbTipo.setValue(u.getTipoUsuario());
    }

    /**
     * Limpa todos os campos e volta ao modo "inserção".
     */
    private void limparFormulario() {
        usuarioSelecionado = null;
        txtNome.clear();
        txtSobrenome.clear();
        txtEmail.clear();
        txtSenha.clear();
        cmbTipo.setValue(null);
        tabela.getSelectionModel().clearSelection();
    }

    private void estilizarCampo(Control campo) {
        campo.setStyle(
                "-fx-background-color: #f8fafc;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;"
                + "-fx-prompt-text-fill: #94a3b8;"
        );
    }

    private Label criarTituloSecao(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #4ca1af; -fx-padding: 8 0 0 0;");
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
}

