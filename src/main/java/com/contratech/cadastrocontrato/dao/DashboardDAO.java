package com.contratech.cadastrocontrato.dao;

import com.contratech.cadastrocontrato.connection.ConnectionFactory;
import com.contratech.cadastrocontrato.model.Contrato;
import com.contratech.cadastrocontrato.model.DashboardResumo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    public DashboardResumo carregarResumo() {
        DashboardResumo resumo = new DashboardResumo();

        try (Connection conn = ConnectionFactory.getConnection()) {
            resumo.setParceirosAtivos(contar(conn, """
                    SELECT COUNT(*) FROM parceiros WHERE ativo = TRUE
                    """));
            resumo.setContratosCadastrados(contar(conn, """
                    SELECT COUNT(*) FROM contratos
                    """));
            resumo.setContratosAtivos(contar(conn, """
                    SELECT COUNT(*) FROM contratos WHERE status = 'ATIVO'
                    """));
            resumo.setContratosVencidos(contar(conn, """
                    SELECT COUNT(*) FROM contratos
                    WHERE status = 'ATIVO'
                      AND data_fim IS NOT NULL
                      AND data_fim < CURRENT_DATE
                    """));
            resumo.setContratosAVencer(contar(conn, """
                    SELECT COUNT(*) FROM contratos
                    WHERE status = 'ATIVO'
                      AND data_fim IS NOT NULL
                      AND data_fim >= CURRENT_DATE
                      AND data_fim <= CURRENT_DATE + INTERVAL '30 days'
                    """));

            Map<String, Integer> tipos = criarMapaPadrao("SERVICO", "FORNECIMENTO", "MISTO", "LOCACAO", "CONSULTORIA");
            tipos.putAll(contarPorCampo(conn, """
                    SELECT COALESCE(tipo, 'SEM TIPO') AS chave, COUNT(*) AS total
                    FROM contratos
                    GROUP BY COALESCE(tipo, 'SEM TIPO')
                    ORDER BY chave
                    """));
            resumo.setContratosPorTipo(tipos);

            Map<String, Integer> status = criarMapaPadrao("ATIVO", "CONCLUIDO", "CANCELADO", "SUSPENSO");
            status.putAll(contarPorCampo(conn, """
                    SELECT status AS chave, COUNT(*) AS total
                    FROM contratos
                    GROUP BY status
                    ORDER BY status
                    """));
            resumo.setContratosPorStatus(status);
            resumo.setVencimentosCriticos(listarVencimentosCriticos(conn));

        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erro ao carregar dashboard: " + e.getMessage());
        }

        return resumo;
    }

    private Map<String, Integer> criarMapaPadrao(String... chaves) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String chave : chaves) {
            mapa.put(chave, 0);
        }
        return mapa;
    }

    private int contar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    private Map<String, Integer> contarPorCampo(Connection conn, String sql) throws SQLException {
        Map<String, Integer> totais = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                totais.put(rs.getString("chave"), rs.getInt("total"));
            }
        }

        return totais;
    }

    private List<Contrato> listarVencimentosCriticos(Connection conn) throws SQLException {
        String sql = """
                SELECT c.id, c.parceiro_id, c.objeto, c.data_fim, c.numero_contrato,
                       c.tipo, c.status, p.razao_social AS parceiro_nome
                FROM contratos c
                INNER JOIN parceiros p ON p.id = c.parceiro_id
                WHERE c.status = 'ATIVO'
                  AND c.data_fim IS NOT NULL
                  AND c.data_fim <= CURRENT_DATE + INTERVAL '30 days'
                ORDER BY c.data_fim ASC
                """;

        List<Contrato> contratos = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Contrato contrato = new Contrato();
                contrato.setId(rs.getInt("id"));
                contrato.setParceiroId(rs.getInt("parceiro_id"));
                contrato.setObjeto(rs.getString("objeto"));
                contrato.setNumeroContrato(rs.getString("numero_contrato"));
                contrato.setTipo(rs.getString("tipo"));
                contrato.setStatus(rs.getString("status"));
                contrato.setParceiroNome(rs.getString("parceiro_nome"));

                Date dataFim = rs.getDate("data_fim");
                contrato.setDataFim(dataFim != null ? dataFim.toLocalDate() : null);

                contratos.add(contrato);
            }
        }

        return contratos;
    }
}
