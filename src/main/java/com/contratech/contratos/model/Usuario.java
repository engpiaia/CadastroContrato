package com.contratech.contratos.model;

import java.time.LocalDateTime;

/**
 * Representa um usuário do sistema.
 * Espelha a tabela 'usuarios' no banco de dados.
 */
public class Usuario {

    private int id;
    private String nome;
    private String sobrenome;
    private String email;
    private String senha;
    private TipoUsuario tipoUsuario;
    private boolean ativo;
    private LocalDateTime criadoEm;

    /**
     * Enum que define os perfis de acesso do sistema.
     * 
     * ADMIN        → CRUD total em tudo
     * RESPONSAVEL  → Cadastra e altera Parceiro, Contrato e Cláusula
     * VISUALIZADOR → Somente leitura
     */
    public enum TipoUsuario {
        ADMIN,
        RESPONSAVEL,
        VISUALIZADOR
    }

    // Construtor vazio — necessário para popular via ResultSet
    public Usuario() {
    }

    // Construtor completo para criação de novo usuário
    public Usuario(String nome, String sobrenome, String email, String senha, TipoUsuario tipoUsuario) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.ativo = true;
    }

    // === Getters e Setters ===

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // Facilita exibição em ComboBox e logs
    @Override
    public String toString() {
        return nome + " " + sobrenome + " (" + tipoUsuario + ")";
    }
}

