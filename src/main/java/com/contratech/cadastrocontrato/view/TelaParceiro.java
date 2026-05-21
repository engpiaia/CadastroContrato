package com.contratech.cadastrocontrato.view;

import com.contratech.cadastrocontrato.dao.ParceiroDAO;
import com.contratech.cadastrocontrato.model.Parceiro;
import com.contratech.cadastrocontrato.model.Usuario;
import com.contratech.cadastrocontrato.util.AlertaUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
 * Tela completa de CRUD de Parceiros (Clientes/Fornecedores).
 *
 * Campos: Razão Social, CNPJ/CPF, Endereço, Cidade, UF, CEP, Telefone, E-mail.
 * Estrutura: formulário à esquerda, tabela à direita.
 */
public class TelaParceiro {

    private final Stage stage;
    private final Usuario usuarioLogado;
    private final ParceiroDAO dao = new ParceiroDAO();

    // Componentes do formulário
    private TextField txtRazaoSocial;
    private TextField txtCnpjCpf;
    private TextField txtEndereco;
    private TextField txtCidade;
    private TextField txtUf;
    private TextField txtCep;
    private TextField txtTelefone;
    private TextField txtEmail;
    private TextField txtPesquisa;

    // Tabela
    private TableView<Parceiro> tabela;
    private ObservableList<Parceiro> listaParceiros;

    // Controle de estado: null = inserindo, preenchido = editando
    private Parceiro parceiroSelecionado = null;

    public TelaParceiro(Stage stage, Usuario usuarioLogado) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
    }

    public void exibir() {
        // === BARRA SUPERIOR ===
        Label lblTitulo = new Label("Gestão de Parceiros");
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

        // Se o usuário for apenas visualizador, desabilita o formulário (visualização somente)
        if (usuarioLogado.getTipoUsuario() == com.contratech.cadastrocontrato.model.Usuario.TipoUsuario.VISUALIZADOR) {
            formulario.setDisable(true);
            formulario.setStyle(formulario.getStyle() + "-fx-opacity: 0.98;");
        }

        // === LAYOUT CENTRAL ===
        HBox centro = new HBox(15, formulario, painelTabela);
        centro.setPadding(new Insets(15));
        centro.setFillHeight(true);
        HBox.setHgrow(painelTabela, Priority.ALWAYS);

        // === LAYOUT PRINCIPAL ===
        BorderPane root = new BorderPane();
        root.setTop(barraSuperior);
        root.setCenter(centro);
        root.setStyle("-fx-background-color: #ECEFF1;");

        Scene scene = new Scene(root, 1050, 600);
        stage.setTitle("Parceiros - CadastroContrato");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);

        atualizarTabela();
    }

    /**
     * Constrói o painel de formulário com todos os campos do parceiro.
     */
    private VBox criarFormulario() {
        Label lblForm = new Label("Dados do Parceiro");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        txtRazaoSocial = new TextField();
        txtRazaoSocial.setPromptText("Nome / Razão Social");
        txtRazaoSocial.setMaxWidth(Double.MAX_VALUE);

        txtCnpjCpf = new TextField();
        txtCnpjCpf.setPromptText("CNPJ ou CPF (somente números)");
        txtCnpjCpf.setMaxWidth(Double.MAX_VALUE);

        txtEndereco = new TextField();
        txtEndereco.setPromptText("Rua, número, complemento");
        txtEndereco.setMaxWidth(Double.MAX_VALUE);

        txtCidade = new TextField();
        txtCidade.setPromptText("Cidade");
        txtCidade.setMaxWidth(Double.MAX_VALUE);

        txtUf = new TextField();
        txtUf.setPromptText("UF (ex: SC)");
        txtUf.setPrefWidth(80);
        // Limita UF a 2 caracteres
        txtUf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 2) {
                txtUf.setText(newVal.substring(0, 2).toUpperCase());
            }
        });

        txtCep = new TextField();
        txtCep.setPromptText("CEP (somente números)");
        txtCep.setMaxWidth(Double.MAX_VALUE);

        // Cidade + UF lado a lado
        HBox linhaCidadeUf = new HBox(10, txtCidade, txtUf);
        HBox.setHgrow(txtCidade, Priority.ALWAYS);

        txtTelefone = new TextField();
        txtTelefone.setPromptText("(49) 99999-9999");
        txtTelefone.setMaxWidth(Double.MAX_VALUE);

        txtEmail = new TextField();
        txtEmail.setPromptText("email@exemplo.com");
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        // === Botões de ação ===
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setPrefWidth(120);
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnSalvar.setOnAction(e -> salvar());

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setPrefWidth(120);
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnExcluir.setOnAction(e -> excluir());

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setPrefWidth(250);
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setStyle("-fx-cursor: hand;");
        btnLimpar.setOnAction(e -> limparFormulario());

        HBox botoesAcao = new HBox(10, btnSalvar, btnExcluir);

        // === Monta o formulário ===
        VBox form = new VBox(8);
        form.setPadding(new Insets(15));
        form.setMinWidth(300);
        form.setMaxWidth(430);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        form.getChildren().addAll(
                lblForm,
                new Separator(),
                new Label("Razão Social / Nome:"), txtRazaoSocial,
                new Label("CNPJ / CPF:"), txtCnpjCpf,
                new Label("Endereço:"), txtEndereco,
                new Label("Cidade / UF:"), linhaCidadeUf,
                new Label("CEP:"), txtCep,
                new Label("Telefone:"), txtTelefone,
                new Label("E-mail:"), txtEmail,
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
        txtPesquisa = new TextField();
        txtPesquisa.setPromptText("Pesquisar por razão social ou CNPJ/CPF...");

        Button btnPesquisar = new Button("Pesquisar");
        btnPesquisar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnPesquisar.setOnAction(e -> pesquisar());

        Button btnListarTodos = new Button("Listar Todos");
        btnListarTodos.setStyle("-fx-cursor: hand;");
        btnListarTodos.setOnAction(e -> atualizarTabela());

        txtPesquisa.setOnAction(e -> pesquisar());

        HBox barraPesquisa = new HBox(10, txtPesquisa, btnPesquisar, btnListarTodos);
        barraPesquisa.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtPesquisa, Priority.ALWAYS);

        // === Tabela ===
        tabela = new TableView<>();
        listaParceiros = FXCollections.observableArrayList();
        tabela.setItems(listaParceiros);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarColunas();

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

    /**
     * Configura as colunas da tabela conforme os campos do modelo Parceiro.
     */
    private void configurarColunas() {
        TableColumn<Parceiro, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(40);

        TableColumn<Parceiro, String> colRazao = new TableColumn<>("Razão Social");
        colRazao.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colRazao.setPrefWidth(180);

        TableColumn<Parceiro, String> colCnpj = new TableColumn<>("CNPJ/CPF");
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpjCpf"));
        colCnpj.setPrefWidth(120);

        TableColumn<Parceiro, String> colCidade = new TableColumn<>("Cidade");
        colCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));
        colCidade.setPrefWidth(100);

        TableColumn<Parceiro, String> colUf = new TableColumn<>("UF");
        colUf.setCellValueFactory(new PropertyValueFactory<>("uf"));
        colUf.setPrefWidth(40);

        TableColumn<Parceiro, String> colTel = new TableColumn<>("Telefone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTel.setPrefWidth(110);

        TableColumn<Parceiro, String> colEmail = new TableColumn<>("E-mail");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(160);

        java.util.List<TableColumn<Parceiro, ?>> colunas = new java.util.ArrayList<>();
        colunas.add(colId);
        colunas.add(colRazao);
        colunas.add(colCnpj);
        colunas.add(colCidade);
        colunas.add(colUf);
        colunas.add(colTel);
        colunas.add(colEmail);
        tabela.getColumns().addAll(colunas);
    }

    // ===================================================================
    // OPERAÇÕES CRUD
    // ===================================================================

    /**
     * Salva (insere ou atualiza) o parceiro.
     */
    private void salvar() {
        String razaoSocial = txtRazaoSocial.getText().trim();
        String cnpjCpf = txtCnpjCpf.getText().trim().replaceAll("[^0-9]", "");
        String endereco = txtEndereco.getText().trim();
        String cidade = txtCidade.getText().trim();
        String uf = txtUf.getText().trim().toUpperCase();
        String cep = txtCep.getText().trim().replaceAll("[^0-9]", "");
        String telefone = txtTelefone.getText().trim();
        String email = txtEmail.getText().trim();

        // --- Validação de campos obrigatórios ---
        if (razaoSocial.isEmpty() || cnpjCpf.isEmpty()) {
            AlertaUtil.aviso("Campos obrigatórios",
                    "Razão Social e CNPJ/CPF são obrigatórios.");
            return;
        }

        // Validação básica do documento (CPF = 11, CNPJ = 14)
        if (cnpjCpf.length() != 11 && cnpjCpf.length() != 14) {
            AlertaUtil.aviso("CNPJ/CPF inválido",
                    "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).");
            return;
        }

        // UF deve ter 2 caracteres se preenchida
        if (!uf.isEmpty() && uf.length() != 2) {
            AlertaUtil.aviso("UF inválida", "O campo UF deve conter 2 letras (ex: SC).");
            return;
        }

        // E-mail opcional, mas se preenchido deve ser válido
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            AlertaUtil.aviso("E-mail inválido", "Informe um e-mail válido.");
            return;
        }

        // Verifica CNPJ/CPF duplicado
        int idAtual = (parceiroSelecionado == null) ? 0 : parceiroSelecionado.getId();
        if (dao.cnpjJaExiste(cnpjCpf, idAtual)) {
            AlertaUtil.erro("Documento duplicado",
                    "Já existe um parceiro cadastrado com este CNPJ/CPF.");
            return;
        }

        // --- Monta o objeto ---
        Parceiro parceiro = new Parceiro();
        parceiro.setRazaoSocial(razaoSocial);
        parceiro.setCnpjCpf(cnpjCpf);
        parceiro.setEndereco(endereco);
        parceiro.setCidade(cidade);
        parceiro.setUf(uf);
        parceiro.setCep(cep);
        parceiro.setTelefone(telefone);
        parceiro.setEmail(email);

        // --- Persiste ---
        boolean isInsercao = (parceiroSelecionado == null);
        boolean sucesso;

        if (isInsercao) {
            sucesso = dao.inserir(parceiro);
        } else {
            parceiro.setId(parceiroSelecionado.getId());
            sucesso = dao.alterar(parceiro);
        }

        if (sucesso) {
            AlertaUtil.info("Sucesso",
                    isInsercao ? "Parceiro cadastrado com sucesso."
                            : "Parceiro atualizado com sucesso.");
            limparFormulario();
            atualizarTabela();
        } else {
            AlertaUtil.erro("Erro", "Não foi possível salvar o parceiro.");
        }
    }

    /**
     * Exclusão lógica do parceiro selecionado.
     */
    private void excluir() {
        if (parceiroSelecionado == null) {
            AlertaUtil.aviso("Nenhum parceiro selecionado",
                    "Selecione um parceiro na tabela para excluir.");
            return;
        }

        boolean confirma = AlertaUtil.confirmar("Confirmar Exclusão",
                "Deseja realmente excluir o parceiro '"
                        + parceiroSelecionado.getRazaoSocial()
                        + "'?\n\nContratos vinculados NÃO serão excluídos.");

        if (confirma) {
            boolean sucesso = dao.excluir(parceiroSelecionado.getId());

            if (sucesso) {
                AlertaUtil.info("Sucesso", "Parceiro excluído com sucesso.");
                limparFormulario();
                atualizarTabela();
            } else {
                AlertaUtil.erro("Erro", "Não foi possível excluir o parceiro.");
            }
        }
    }

    /**
     * Pesquisa parceiros por razão social ou CNPJ/CPF.
     * Se o termo for numérico, pesquisa por CNPJ. Senão, por nome.
     */
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();

        if (termo.isEmpty()) {
            atualizarTabela();
            return;
        }

        // Se contém apenas números, pesquisa por CNPJ/CPF
        if (termo.matches("[0-9]+")) {
            listaParceiros.setAll(dao.pesquisarPorCnpj(termo));
        } else {
            listaParceiros.setAll(dao.pesquisarPorNome(termo));
        }
    }

    /**
     * Recarrega toda a tabela.
     */
    private void atualizarTabela() {
        listaParceiros.setAll(dao.listarTodos());
        txtPesquisa.clear();
    }

    /**
     * Preenche o formulário com dados do parceiro selecionado na tabela.
     */
    private void preencherFormulario(Parceiro p) {
        parceiroSelecionado = p;
        txtRazaoSocial.setText(p.getRazaoSocial());
        txtCnpjCpf.setText(p.getCnpjCpf());
        txtEndereco.setText(p.getEndereco() != null ? p.getEndereco() : "");
        txtCidade.setText(p.getCidade() != null ? p.getCidade() : "");
        txtUf.setText(p.getUf() != null ? p.getUf() : "");
        txtCep.setText(p.getCep() != null ? p.getCep() : "");
        txtTelefone.setText(p.getTelefone() != null ? p.getTelefone() : "");
        txtEmail.setText(p.getEmail() != null ? p.getEmail() : "");
    }

    /**
     * Limpa todos os campos e volta ao modo inserção.
     */
    private void limparFormulario() {
        parceiroSelecionado = null;
        txtRazaoSocial.clear();
        txtCnpjCpf.clear();
        txtEndereco.clear();
        txtCidade.clear();
        txtUf.clear();
        txtCep.clear();
        txtTelefone.clear();
        txtEmail.clear();
        tabela.getSelectionModel().clearSelection();
    }
}
