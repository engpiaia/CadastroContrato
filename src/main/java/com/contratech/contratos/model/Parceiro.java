package com.contratech.contratos.model;

import java.time.LocalDateTime;

/**
 * Representa um parceiro (cliente ou fornecedor).
 * Espelha a tabela 'parceiros' no banco de dados.
 */
public class Parceiro {

    private int id;
    private String razaoSocial;
    private String cnpjCpf;
    private String endereco;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
    private String email;
    private boolean ativo;
    private LocalDateTime criadoEm;

    public Parceiro() {
    }

    public Parceiro(String razaoSocial, String cnpjCpf, String endereco,
                    String cidade, String uf, String cep,
                    String telefone, String email) {
        this.razaoSocial = razaoSocial;
        this.cnpjCpf = cnpjCpf;
        this.endereco = endereco;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
        this.telefone = telefone;
        this.email = email;
        this.ativo = true;
    }

    // === Getters e Setters ===

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpjCpf() {
        return cnpjCpf;
    }

    public void setCnpjCpf(String cnpjCpf) {
        this.cnpjCpf = cnpjCpf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // Usado em ComboBox de seleção de parceiro na tela de contratos
    @Override
    public String toString() {
        return razaoSocial + " (" + cnpjCpf + ")";
    }
}

