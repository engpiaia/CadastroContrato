package com.contratech.contratos.model;

import java.time.LocalDateTime;

/**
 * Representa uma cláusula vinculada a um contrato.
 *
 * Cada contrato pode ter N cláusulas, numeradas sequencialmente.
 * A FK contrato_id garante integridade referencial com a tabela contratos.
 */
public class Clausula {

    private int id;
    private int contratoId;
    private int numero;
    private String descricao;
    private LocalDateTime criadoEm;

    // Campo auxiliar (vem do JOIN, não persiste)
    private String numeroContrato;

    // ==================== CONSTRUTORES ====================

    public Clausula() {
    }

    public Clausula(int contratoId, int numero, String descricao) {
        this.contratoId = contratoId;
        this.numero = numero;
        this.descricao = descricao;
    }

    // ==================== GETTERS E SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContratoId() {
        return contratoId;
    }

    public void setContratoId(int contratoId) {
        this.contratoId = contratoId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    @Override
    public String toString() {
        return "Cláusula " + numero;
    }
}
