package com.contratech.contratos.dto;

import com.contratech.contratos.model.Contrato;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardResumo {

    private int parceirosAtivos;
    private int contratosCadastrados;
    private int contratosAtivos;
    private int contratosVencidos;
    private int contratosAVencer;
    private Map<String, Integer> contratosPorTipo = new LinkedHashMap<>();
    private Map<String, Integer> contratosPorStatus = new LinkedHashMap<>();
    private List<Contrato> vencimentosCriticos = new ArrayList<>();

    public int getParceirosAtivos() {
        return parceirosAtivos;
    }

    public void setParceirosAtivos(int parceirosAtivos) {
        this.parceirosAtivos = parceirosAtivos;
    }

    public int getContratosCadastrados() {
        return contratosCadastrados;
    }

    public void setContratosCadastrados(int contratosCadastrados) {
        this.contratosCadastrados = contratosCadastrados;
    }

    public int getContratosAtivos() {
        return contratosAtivos;
    }

    public void setContratosAtivos(int contratosAtivos) {
        this.contratosAtivos = contratosAtivos;
    }

    public int getContratosVencidos() {
        return contratosVencidos;
    }

    public void setContratosVencidos(int contratosVencidos) {
        this.contratosVencidos = contratosVencidos;
    }

    public int getContratosAVencer() {
        return contratosAVencer;
    }

    public void setContratosAVencer(int contratosAVencer) {
        this.contratosAVencer = contratosAVencer;
    }

    public Map<String, Integer> getContratosPorTipo() {
        return contratosPorTipo;
    }

    public void setContratosPorTipo(Map<String, Integer> contratosPorTipo) {
        this.contratosPorTipo = contratosPorTipo;
    }

    public Map<String, Integer> getContratosPorStatus() {
        return contratosPorStatus;
    }

    public void setContratosPorStatus(Map<String, Integer> contratosPorStatus) {
        this.contratosPorStatus = contratosPorStatus;
    }

    public List<Contrato> getVencimentosCriticos() {
        return vencimentosCriticos;
    }

    public void setVencimentosCriticos(List<Contrato> vencimentosCriticos) {
        this.vencimentosCriticos = vencimentosCriticos;
    }
}
