package com.contratech.cadastrocontrato.view;

import com.contratech.cadastrocontrato.dao.UsuarioDAO;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.model.Usuario.TipoUsuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;
import com.contratech.cadastrocontrato.util.SenhaUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
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
        // === BARRA SUPERIOR ===
        Label lblTitulo = new Label("Gestão de Usuários");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button btnVoltar = new Button("← Menu");
        btnVoltar.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-cursor: hand;");
        btnVoltar.setOnAction(e -> {
            TelaPrincipal tela = new TelaPrincipal(stage, usuarioLogado);
            tela.exibir();
        });

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        HBox barraSuperior = new HBox(15, btnVoltar, lblTitulo, espacador);
        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setPadding(new Insets(12, 20, 12, 20));
        barraSuperior.setStyle("-fx-background-color: white; "
                             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // === FORMULÁRIO (lado esquerdo) ===
        VBox formulario = criarFormulario();

        // === TABELA (lado direito) ===
        VBox painelTabela = criarPainelTabela();

        // === LAYOUT CENTRAL ===
        HBox centro = new HBox(15, formulario, painelTabela);
        centro.setPadding(new Insets(15));
        HBox.setHgrow(painelTabela, Priority.ALWAYS);

        // === LAYOUT PRINCIPAL ===
        BorderPane root = new BorderPane();
        root.setTop(barraSuperior);
        root.setCenter(centro);
        root.setStyle("-fx-background-color: #ECEFF1;");

        Scene scene = new Scene(root, 900, 550);
        stage.setTitle("Usuários - CadastroContrato");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();

        // Carrega dados iniciais
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

        txtSobrenome = new TextField();
        txtSobrenome.setPromptText("Sobrenome");

        txtEmail = new TextField();
        txtEmail.setPromptText("email@exemplo.com");

        txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha (mín. 6 caracteres)");

        cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoUsuario.values()));
        cmbTipo.setPromptText("Tipo de Usuário");
        cmbTipo.setPrefWidth(250);

        // === Botões de ação ===
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setPrefWidth(120);
        btnSalvar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                         + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnSalvar.setOnAction(e -> salvar());

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setPrefWidth(120);
        btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; "
                          + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnExcluir.setOnAction(e -> excluir());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setPrefWidth(250);
        btnLimpar.setStyle("-fx-cursor: hand;");
        btnLimpar.setOnAction(e -> limparFormulario());

        HBox botoesAcao = new HBox(10, btnSalvar, btnExcluir);

        // === Monta o formulário ===
        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        form.setPrefWidth(280);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        form.getChildren().addAll(
            lblForm,
            new Separator(),
            new Label("Nome:"), txtNome,
            new Label("Sobrenome:"), txtSobrenome,
            new Label("E-mail:"), txtEmail,
            new Label("Senha:"), txtSenha,
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

        Button btnPesquisar = new Button("Pesquisar");
        btnPesquisar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnPesquisar.setOnAction(e -> pesquisar());

        Button btnListarTodos = new Button("Listar Todos");
        btnListarTodos.setStyle("-fx-cursor: hand;");
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

        @SuppressWarnings("unchecked")
        var colunas = tabela.getColumns();
        colunas.addAll(colId, colNome, colSobrenome, colEmail, colTipo);

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
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

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
}

