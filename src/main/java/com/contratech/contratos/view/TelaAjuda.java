package com.contratech.contratos.view;

import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.util.ui.AjudaUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class TelaAjuda {

    private final Stage stage;
    private final Usuario usuarioLogado;
    private final String topicoInicial;
    private final Runnable acaoVoltar;
    private final Map<String, String> topicos = criarTopicos();

    private ListView<String> listaTopicos;
    private TextArea txtConteudo;
    private ObservableList<String> itensFiltrados;

    public TelaAjuda(Stage stage, Usuario usuarioLogado, String topicoInicial, Runnable acaoVoltar) {
        this.stage = stage;
        this.usuarioLogado = usuarioLogado;
        this.topicoInicial = topicoInicial;
        this.acaoVoltar = acaoVoltar;
    }

    public void exibir() {
        Label lblTitulo = new Label("Ajuda do Sistema");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTitulo.setStyle("-fx-text-fill: white;");

        Label lblPerfil = new Label(usuarioLogado != null
                ? usuarioLogado.getTipoUsuario().toString()
                : "LOGIN");
        lblPerfil.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);"
                + "-fx-padding: 5 10;"
                + "-fx-background-radius: 20;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 20;"
                + "-fx-padding: 6 14;"
                + "-fx-font-weight: bold;"
        );
        btnVoltar.setOnAction(e -> voltar());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox header = new HBox(12, lblTitulo, lblPerfil, espaco, btnVoltar);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setPrefHeight(78);
        header.setMinHeight(78);
        header.setMaxHeight(78);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10,0,0,3);"
        );

        TextField txtPesquisa = new TextField();
        txtPesquisa.setPromptText("Pesquisar na ajuda...");

        itensFiltrados = FXCollections.observableArrayList(topicos.keySet());
        listaTopicos = new ListView<>(itensFiltrados);
        listaTopicos.setPrefWidth(270);
        listaTopicos.setMinWidth(240);

        txtConteudo = new TextArea();
        txtConteudo.setEditable(false);
        txtConteudo.setWrapText(true);
        txtConteudo.setFont(Font.font("Segoe UI", 14));
        txtConteudo.setStyle("-fx-control-inner-background: white; -fx-text-fill: #2c3e50;");

        listaTopicos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                exibirTopico(novo);
            }
        });

        txtPesquisa.textProperty().addListener((obs, antigo, novo) -> filtrarTopicos(novo));

        VBox lateral = new VBox(10,
                criarTituloSecao("Tópicos"),
                txtPesquisa,
                listaTopicos
        );
        lateral.setPadding(new Insets(16));
        lateral.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(listaTopicos, Priority.ALWAYS);

        VBox conteudo = new VBox(10,
                criarTituloSecao("Conteúdo"),
                new Separator(),
                txtConteudo
        );
        conteudo.setPadding(new Insets(16));
        conteudo.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(txtConteudo, Priority.ALWAYS);

        HBox centro = new HBox(16, lateral, conteudo);
        centro.setPadding(new Insets(20));
        HBox.setHgrow(conteudo, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(centro);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scroll);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef3f8, #dce6f1);");

        Scene scene = new Scene(root, 1100, 700);
        AjudaUtil.registrarAtalhoF1(scene, stage, usuarioLogado, topicoInicial, acaoVoltar);

        stage.setTitle("Ajuda - CadastroContrato");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        selecionarTopico(topicoInicial);
    }

    private Label criarTituloSecao(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        label.setStyle("-fx-text-fill: #2c3e50;");
        return label;
    }

    private void filtrarTopicos(String termo) {
        String filtro = termo == null ? "" : termo.trim().toLowerCase();
        itensFiltrados.clear();

        for (Map.Entry<String, String> entrada : topicos.entrySet()) {
            String titulo = entrada.getKey().toLowerCase();
            String conteudo = entrada.getValue().toLowerCase();
            if (filtro.isEmpty() || titulo.contains(filtro) || conteudo.contains(filtro)) {
                itensFiltrados.add(entrada.getKey());
            }
        }

        if (!itensFiltrados.isEmpty()) {
            listaTopicos.getSelectionModel().select(0);
        } else {
            txtConteudo.setText("Nenhum tópico encontrado para a pesquisa informada.");
        }
    }

    private void selecionarTopico(String topico) {
        String alvo = topico != null && topicos.containsKey(topico)
                ? topico
                : "Visão geral";

        listaTopicos.getSelectionModel().select(alvo);
        exibirTopico(alvo);
    }

    private void exibirTopico(String topico) {
        String conteudo = topicos.get(topico);
        txtConteudo.setText((conteudo != null ? conteudo : "Tópico não encontrado."));
        txtConteudo.positionCaret(0);
    }

    private void voltar() {
        if (acaoVoltar != null) {
            acaoVoltar.run();
        } else if (usuarioLogado != null) {
            new TelaPrincipal(stage, usuarioLogado).exibir();
        } else {
            new TelaLogin(stage).exibir();
        }
    }

    private Map<String, String> criarTopicos() {
        Map<String, String> mapa = new LinkedHashMap<>();

        mapa.put("Visão geral", """
                VISÃO GERAL DO CADASTROCONTRATO

                O CadastroContrato é um sistema desktop para organizar parceiros, contratos, cláusulas e usuários.

                O fluxo comum de uso é:
                1. Entrar no sistema com e-mail e senha.
                2. Cadastrar parceiros.
                3. Cadastrar contratos vinculados aos parceiros.
                4. Registrar as cláusulas de cada contrato.
                5. Acompanhar alertas, vencimentos e totais no dashboard.

                Use F1 em qualquer tela para abrir esta ajuda. O botão Ajuda no topo das telas faz a mesma coisa.
                """);

        mapa.put("Login", """
                LOGIN

                A tela de login é a porta de entrada do sistema.

                Como entrar:
                1. Digite seu e-mail no campo E-mail.
                2. Digite sua senha.
                3. Clique em Entrar ou pressione Enter no campo de senha.

                Se o login falhar:
                - Confira se o e-mail foi digitado corretamente.
                - Confira a senha.
                - Verifique se o usuário está ativo no banco.
                - Se aparecer erro de conexão, confira as variáveis DB_HOST, DB_PORT, DB_NAME, DB_USER e DB_PASSWORD.

                Perfis:
                - ADMIN: acessa todos os cadastros, inclusive usuários.
                - RESPONSÁVEL: opera parceiros e contratos.
                - VISUALIZADOR: consulta informações, mas não edita cadastros.
                """);

        mapa.put("Tela principal e dashboard", """
                TELA PRINCIPAL E DASHBOARD

                A tela principal aparece depois do login e resume a situação do sistema.

                Indicadores:
                - Parceiros ativos: total de parceiros com ativo.
                - Contratos cadastrados: total geral de contratos.
                - Contratos ativos: contratos com status ATIVO.
                - Contratos vencidos: contratos ATIVO com data fim anterior a hoje.
                - A vencer em 30 dias: contratos ATIVO que vencem de hoje até os próximos 30 dias.

                Cards centrais:
                - Contratos por tipo: mostra totais de SERVIÇO, FORNECIMENTO, MISTO, LOCAÇÃO e CONSULTORIA.
                - Contratos por status: mostra totais de ATIVO, CONCLUÍDO, CANCELADO e SUSPENSO.
                - Vencimentos críticos: lista contratos vencidos ou perto do vencimento, ordenados pela data fim.

                Ações rápidas:
                - Usuários: abre gestão de usuários. Apenas ADMIN pode acessar.
                - Parceiros: abre cadastro de parceiros.
                - Contratos: abre cadastro de contratos.

                O botão Sair encerra a sessão e volta ao login.
                """);

        mapa.put("Cadastro de parceiros", """
                CADASTRO DE PARCEIROS

                Parceiros representam clientes, fornecedores ou qualquer pessoa/empresa vinculada a contratos.

                Como cadastrar:
                1. Na tela principal, clique em Parceiros.
                2. Preencha Razão Social / Nome.
                3. Preencha CNPJ ou CPF somente com números.
                4. Se for CNPJ com 14 dígitos, o sistema tenta consultar dados automaticamente.
                5. Revise endereço, cidade, UF, CEP, telefone e e-mail.
                6. Clique em Salvar.

                Consulta automática de CNPJ:
                - Acontece quando o campo CNPJ/CPF perde o foco e contém 14 dígitos.
                - O sistema preenche apenas campos vazios.
                - Se a consulta falhar, preencha manualmente.

                Como editar:
                1. Selecione o parceiro na tabela.
                2. Altere os campos desejados.
                3. Clique em Salvar.

                Como excluir:
                1. Selecione o parceiro na tabela.
                2. Clique em Excluir.
                3. Confirme a operação.

                Importante:
                - A exclusão é lógica: o parceiro fica com ativo = FALSE.
                - Contratos vinculados não são excluídos.
                - Documento duplicado não é permitido para parceiro ativo.
                """);

        mapa.put("Cadastro de contratos", """
                CADASTRO DE CONTRATOS

                Contratos registram acordos vinculados a parceiros.

                Como cadastrar:
                1. Na tela principal, clique em Contratos.
                2. Informe o número do contrato.
                3. Selecione o parceiro.
                4. Preencha o objeto do contrato.
                5. Informe descrição, tipo, valor, multa, forma de pagamento, datas, status e observações.
                6. Clique em Salvar.

                Campos principais:
                - Número do contrato: identificação interna ou oficial.
                - Parceiro: empresa/pessoa vinculada.
                - Objeto: resumo do que o contrato trata.
                - Descrição: detalhes adicionais.
                - Tipo: SERVIÇO, FORNECIMENTO, MISTO, LOCAÇÃO ou CONSULTORIA.
                - Valor: valor global do contrato.
                - Multa: valor de multa, se houver.
                - Forma de pagamento: A_VISTA, PARCELADO, MENSAL ou RECORRENTE.
                - Data início e Data fim: use dd/MM/yyyy.
                - Status: ATIVO, CONCLUÍDO, CANCELADO ou SUSPENSO.

                Regras:
                - Número, parceiro, objeto, tipo, forma de pagamento e status são obrigatórios.
                - Data fim não pode ser anterior a data início.
                - Valores devem ser numéricos. Use ponto ou vírgula para casas decimais.

                Como editar:
                1. Selecione o contrato na tabela.
                2. Os dados serão carregados no formulário.
                3. Altere o necessário.
                4. Clique em Salvar.

                Como excluir:
                1. Selecione o contrato.
                2. Clique em Excluir.
                3. Confirme. As cláusulas vinculadas também serão removidas.
                """);

        mapa.put("Cláusulas do contrato", """
                CLÁUSULAS DO CONTRATO

                O painel de cláusulas fica dentro da tela de Contratos.

                Como usar:
                1. Cadastre ou selecione um contrato.
                2. O painel Cláusulas do Contrato será habilitado.
                3. Informe o número da cláusula.
                4. Informe a descrição/texto da cláusula.
                5. Clique em Salvar Cláusula.

                Regras:
                - É necessário selecionar um contrato antes.
                - O número da cláusula deve ser inteiro e maior que zero.
                - Não pode haver duas cláusulas com o mesmo número no mesmo contrato.
                - O sistema sugere o próximo número disponível.

                Como editar:
                1. Selecione uma cláusula na mini-tabela.
                2. Altere número ou descrição.
                3. Clique em Salvar Cláusula.

                Como excluir:
                1. Selecione a cláusula.
                2. Clique em Excluir Cláusula.
                3. Confirme a operação.
                """);

        mapa.put("Gestão de usuários", """
                GESTÃO DE USUÁRIOS

                Esta tela é restrita a usuários ADMIN.

                Como cadastrar usuário:
                1. Clique em Usuários na tela principal.
                2. Informe nome, sobrenome e e-mail.
                3. Informe uma senha com no mínimo 6 caracteres.
                4. Selecione o tipo de usuário.
                5. Clique em Salvar.

                Como editar:
                1. Selecione o usuário na tabela.
                2. Altere nome, sobrenome, e-mail ou perfil.
                3. Se quiser manter a senha atual, deixe o campo senha vazio.
                4. Se quiser trocar a senha, digite uma nova senha.
                5. Clique em Salvar.

                Como excluir:
                1. Selecione o usuário.
                2. Clique em Excluir.
                3. Confirme a operação.

                Regras:
                - O ADMIN não pode excluir seu próprio usuário.
                - E-mail duplicado não é permitido.
                - Senhas ficam armazenadas como hash SHA-256.
                """);

        mapa.put("Pesquisa e tabelas", """
                PESQUISA E TABELAS

                As telas de parceiros, contratos e usuários possuem campo de pesquisa acima da tabela.

                Parceiros:
                - Se o termo contém apenas números, pesquisa por CNPJ/CPF.
                - Caso contrário, pesquisa por razão social/nome.

                Contratos:
                - Pesquisa por número do contrato, objeto ou nome do parceiro.

                Usuários:
                - Pesquisa por nome.

                Dicas:
                - Pressione Enter no campo de pesquisa para pesquisar.
                - Clique em Listar Todos para limpar o filtro.
                - Clique em uma linha da tabela para carregar os dados no formulário.
                """);

        mapa.put("Alertas de vencimento", """
                ALERTAS DE VENCIMENTO

                Ao entrar na tela principal, o sistema verifica contratos vencidos ou próximos do vencimento.

                Entram no alerta:
                - Contratos com status ATIVO.
                - Contratos com data fim preenchida.
                - Contratos vencidos ou com vencimento nos próximos 30 dias.

                Definições:
                - Vencido: data fim menor que a data atual.
                - Próximo do vencimento: data fim entre hoje e os próximos 30 dias.

                O mesmo critério aparece no dashboard:
                - Contratos vencidos.
                - A vencer em 30 dias.
                - Vencimentos críticos.
                """);

        mapa.put("Perfis de acesso", """
                PERFIS DE ACESSO

                ADMIN:
                - Acessa dashboard.
                - Gerencia parceiros.
                - Gerencia contratos e cláusulas.
                - Gerencia usuários.

                RESPONSÁVEL:
                - Acessa dashboard.
                - Gerencia parceiros.
                - Gerencia contratos e cláusulas.
                - Não acessa gestão de usuários.

                VISUALIZADOR:
                - Acessa dashboard.
                - Consulta parceiros e contratos.
                - Formulários de edição ficam bloqueados.
                - Não acessa gestão de usuários.
                """);

        mapa.put("Boas práticas", """
                BOAS PRÁTICAS DE OPERAÇÃO

                - Cadastre o parceiro antes de cadastrar o contrato.
                - Use números de contrato padronizados, como 2026/001 ou CT-0001.
                - Mantenha datas de início e fim preenchidas corretamente.
                - Use status ATIVO apenas para contratos vigentes.
                - Ao finalizar um contrato, altere o status para CONCLUÍDO.
                - Use CANCELADO para contratos cancelados e SUSPENSO para contratos temporariamente parados.
                - Revise os vencimentos críticos no dashboard periodicamente.
                - Evite excluir registros sem conferir se foram selecionados corretamente.
                """);

        mapa.put("Problemas comuns", """
                PROBLEMAS COMUNS

                Não consigo entrar:
                - Verifique e-mail e senha.
                - Confirme se o banco está ativo.
                - Confirme o arquivo .env ou variáveis de ambiente.

                Não aparece parceiro no contrato:
                - Verifique se o parceiro está cadastrado e ativo.

                CNPJ não consultou:
                - Use somente números.
                - Confira se tem 14 dígitos.
                - Verifique internet e limite da API pública.

                Contrato não salva:
                - Confira campos obrigatórios.
                - Confira formato das datas: dd/MM/yyyy.
                - Confira se data fim não é anterior a data início.
                - Confira se valores são numéricos.

                Não consigo acessar Usuários:
                - Apenas ADMIN pode abrir essa tela.
                """);

        return mapa;
    }
}
