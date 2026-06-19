package com.contratech.contratos.dao;

import com.contratech.contratos.config.ConnectionFactory;
import com.contratech.contratos.model.Contrato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratoDAO {

    // ==================== INSERIR ====================
    public boolean inserir(Contrato c) {
        String sql = """
            INSERT INTO contratos
                (parceiro_id, objeto, valor_contrato, multa, data_inicio, data_fim,
                 criado_em, numero_contrato, descricao, tipo, forma_pagamento,
                 observacoes, atualizado_em, status)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, NOW(), ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, c.getParceiroId());
            ps.setString(2, c.getObjeto());
            ps.setBigDecimal(3, c.getValorContrato());
            ps.setBigDecimal(4, c.getMulta());
            ps.setDate(5, Date.valueOf(c.getDataInicio()));
            ps.setDate(6, c.getDataFim() != null ? Date.valueOf(c.getDataFim()) : null);
            ps.setString(7, c.getNumeroContrato());
            ps.setString(8, c.getDescricao());
            ps.setString(9, c.getTipo());
            ps.setString(10, c.getFormaPagamento());
            ps.setString(11, c.getObservacoes());
            ps.setString(12, c.getStatus());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir contrato: " + e.getMessage());
            return false;
        }
    }

    // ==================== ATUALIZAR ====================
    public boolean atualizar(Contrato c) {
        String sql = """
            UPDATE contratos SET
                parceiro_id = ?, objeto = ?, valor_contrato = ?, multa = ?,
                data_inicio = ?, data_fim = ?, numero_contrato = ?, descricao = ?,
                tipo = ?, forma_pagamento = ?, observacoes = ?, atualizado_em = NOW(),
                status = ?
            WHERE id = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, c.getParceiroId());
            ps.setString(2, c.getObjeto());
            ps.setBigDecimal(3, c.getValorContrato());
            ps.setBigDecimal(4, c.getMulta());
            ps.setDate(5, Date.valueOf(c.getDataInicio()));
            ps.setDate(6, c.getDataFim() != null ? Date.valueOf(c.getDataFim()) : null);
            ps.setString(7, c.getNumeroContrato());
            ps.setString(8, c.getDescricao());
            ps.setString(9, c.getTipo());
            ps.setString(10, c.getFormaPagamento());
            ps.setString(11, c.getObservacoes());
            ps.setString(12, c.getStatus());
            ps.setInt(13, c.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar contrato: " + e.getMessage());
            return false;
        }
    }

    // ==================== EXCLUIR ====================
    public boolean excluir(int id) {
        String sql = "DELETE FROM contratos WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir contrato: " + e.getMessage());
            return false;
        }
    }

    // ==================== LISTAR TODOS ====================
    public List<Contrato> listarTodos() {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            ORDER BY c.id DESC
            """;

        List<Contrato> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearContrato(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar contratos: " + e.getMessage());
        }

        return lista;
    }

    // ==================== BUSCAR POR ID ====================
    public Contrato buscarPorId(int id) {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.id = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearContrato(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar contrato: " + e.getMessage());
        }

        return null;
    }

    // ==================== BUSCAR POR NÚMERO ====================
    public Contrato buscarPorNumero(String numero) {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.numero_contrato = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearContrato(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar contrato por número: " + e.getMessage());
        }

        return null;
    }

    // ==================== PESQUISAR POR TERMO ====================
    public List<Contrato> pesquisar(String termo) {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE LOWER(c.numero_contrato) LIKE LOWER(?)
               OR LOWER(c.objeto) LIKE LOWER(?)
               OR LOWER(p.razao_social) LIKE LOWER(?)
            ORDER BY c.id DESC
            """;

        List<Contrato> lista = new ArrayList<>();
        String filtro = "%" + termo + "%";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearContrato(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao pesquisar contratos: " + e.getMessage());
        }

        return lista;
    }

    // ==================== BUSCAR POR PARCEIRO ====================
    public List<Contrato> buscarPorParceiro(int parceiroId) {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.parceiro_id = ?
            ORDER BY c.id DESC
            """;

        List<Contrato> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, parceiroId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearContrato(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar contratos por parceiro: " + e.getMessage());
        }

        return lista;
    }

    // ==================== LISTAR POR STATUS ====================
    public List<Contrato> listarPorStatus(String status) {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.status = ?
            ORDER BY c.id DESC
            """;

        List<Contrato> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearContrato(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar contratos por status: " + e.getMessage());
        }

        return lista;
    }

    public List<Contrato> listarContratosVencidos() {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.status = 'ATIVO'
              AND c.data_fim IS NOT NULL
              AND c.data_fim < CURRENT_DATE
            ORDER BY c.data_fim ASC
            """;

        List<Contrato> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearContrato(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar contratos vencidos: " + e.getMessage());
        }

        return lista;
    }

    public List<Contrato> listarContratosAVencer() {
        String sql = """
            SELECT c.*, p.razao_social AS parceiro_nome
            FROM contratos c
            INNER JOIN parceiros p ON p.id = c.parceiro_id
            WHERE c.status = 'ATIVO'
              AND c.data_fim IS NOT NULL
              AND c.data_fim >= CURRENT_DATE
              AND c.data_fim <= CURRENT_DATE + INTERVAL '30 days'
            ORDER BY c.data_fim ASC
            """;

        List<Contrato> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearContrato(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar contratos a vencer: " + e.getMessage());
        }

        return lista;
    }

    // ==================== VERIFICAR NÚMERO DUPLICADO ====================
    public boolean numeroJaExiste(String numero, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM contratos WHERE numero_contrato = ? AND id != ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            ps.setInt(2, idIgnorar);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao verificar número duplicado: " + e.getMessage());
        }

        return false;
    }

    // ==================== MAPEAMENTO DO RESULTSET ====================
    private Contrato mapearContrato(ResultSet rs) throws SQLException {
        Contrato c = new Contrato();

        c.setId(rs.getInt("id"));
        c.setParceiroId(rs.getInt("parceiro_id"));
        c.setObjeto(rs.getString("objeto"));
        c.setValorContrato(rs.getBigDecimal("valor_contrato"));
        c.setMulta(rs.getBigDecimal("multa"));

        Date dataInicio = rs.getDate("data_inicio");
        c.setDataInicio(dataInicio != null ? dataInicio.toLocalDate() : null);

        Date dataFim = rs.getDate("data_fim");
        c.setDataFim(dataFim != null ? dataFim.toLocalDate() : null);

        Timestamp criadoEm = rs.getTimestamp("criado_em");
        c.setCriadoEm(criadoEm != null ? criadoEm.toLocalDateTime() : null);

        c.setNumeroContrato(rs.getString("numero_contrato"));
        c.setDescricao(rs.getString("descricao"));
        c.setTipo(rs.getString("tipo"));
        c.setFormaPagamento(rs.getString("forma_pagamento"));
        c.setObservacoes(rs.getString("observacoes"));

        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        c.setAtualizadoEm(atualizadoEm != null ? atualizadoEm.toLocalDateTime() : null);

        c.setStatus(rs.getString("status"));
        c.setParceiroNome(rs.getString("parceiro_nome"));

        return c;
    }
}
