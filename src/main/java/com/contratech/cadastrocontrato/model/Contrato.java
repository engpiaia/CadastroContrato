// com.contratech.cadastrocontrato.model.Contrato.java
package com.contratech.cadastrocontrato.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Contrato {

    private Integer id;
    private Integer parceiroId;
    private String objeto;
    private BigDecimal valorContrato;
    private BigDecimal multa;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDateTime criadoEm;
    private String numeroContrato;
    private String descricao;
    private String tipo;           // SERVICO, FORNECIMENTO, MISTO
    private String formaPagamento; // A_VISTA, PARCELADO, MENSAL
    private String observacoes;
    private LocalDateTime atualizadoEm;
    private String status;         // ATIVO, CONCLUIDO, CANCELADO, SUSPENSO

    // --- Campo auxiliar para exibição na tela ---
    private String parceiroNome;

    // --- Construtor vazio ---
    public Contrato() {
    }

    // --- Getters e Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParceiroId() {
        return parceiroId;
    }

    public void setParceiroId(Integer parceiroId) {
        this.parceiroId = parceiroId;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public BigDecimal getValorContrato() {
        return valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParceiroNome() {
        return parceiroNome;
    }

    public void setParceiroNome(String parceiroNome) {
        this.parceiroNome = parceiroNome;
    }
    // Retorna o nome do parceiro (compatibilidade com TelaPrincipal)
    public String getParceiroRazaoSocial() {
        return this.parceiroNome;
    }

    // Calcula os dias restantes até o vencimento (data_fim)
    public long getPrazoRestante() {
        if (this.dataFim == null) {
            return Long.MAX_VALUE; // Sem data fim = sem vencimento
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.dataFim);
    }

    @Override
    public String toString() {
        return numeroContrato + " - " + objeto;
    }
}
