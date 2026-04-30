package com.contratech.cadastrocontrato.dao;

import com.contratech.cadastrocontrato.connection.ConnectionFactory;
import com.contratech.cadastrocontrato.model.Clausula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD na tabela clausulas.
 *
 * Todas as cláusulas são vinculadas a um contrato (contrato_id).
 * O campo 'numero' representa a ordem da cláusula dentro do contrato.
 */
public class ClausulaDAO {

    // ==================== INSERIR ====================
    public boolean inserir(Clausula c) {
        String sql = """
            INSERT INTO clausulas (contrato_id, numero, descricao, criado_em)
            VALUES (?, ?, ?, NOW())
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, c.getContratoId());
            ps.setInt(2, c.getNumero());
            ps.setString(3, c.getDescricao());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir cláusula: " + e.getMessage());
            return false;
        }
    }

    // ==================== ATUALIZAR ====================
    public boolean atualizar(Clausula c) {
        String sql = """
            UPDATE clausulas SET
                numero = ?, descricao = ?
            WHERE id = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, c.getNumero());
            ps.setString(2, c.getDescricao());
            ps.setInt(3, c.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cláusula: " + e.getMessage());
            return false;
        }
    }

    // ==================== EXCLUIR ====================
    public boolean excluir(int id) {
        String sql = "DELETE FROM clausulas WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir cláusula: " + e.getMessage());
            return false;
        }
    }

    // ==================== LISTAR POR CONTRATO ====================
    /**
     * Retorna todas as cláusulas de um contrato, ordenadas pelo número.
     */
    public List<Clausula> listarPorContrato(int contratoId) {
        String sql = """
            SELECT cl.*, c.numero_contrato
            FROM clausulas cl
            INNER JOIN contratos c ON c.id = cl.contrato_id
            WHERE cl.contrato_id = ?
            ORDER BY cl.numero ASC
            """;

        List<Clausula> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contratoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar cláusulas: " + e.getMessage());
        }

        return lista;
    }

    // ==================== LISTAR TODAS (com nº contrato) ====================
    /**
     * Lista todas as cláusulas do sistema com o número do contrato vinculado.
     * Usado na tela geral de cláusulas.
     */
    public List<Clausula> listarTodas() {
        String sql = """
            SELECT cl.*, c.numero_contrato
            FROM clausulas cl
            INNER JOIN contratos c ON c.id = cl.contrato_id
            ORDER BY c.numero_contrato ASC, cl.numero ASC
            """;

        List<Clausula> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar todas as cláusulas: " + e.getMessage());
        }

        return lista;
    }

    // ==================== PESQUISAR ====================
    /**
     * Pesquisa cláusulas por número do contrato ou texto da descrição.
     */
    public List<Clausula> pesquisar(String termo) {
        String sql = """
            SELECT cl.*, c.numero_contrato
            FROM clausulas cl
            INNER JOIN contratos c ON c.id = cl.contrato_id
            WHERE LOWER(c.numero_contrato) LIKE LOWER(?)
               OR LOWER(cl.descricao) LIKE LOWER(?)
            ORDER BY c.numero_contrato ASC, cl.numero ASC
            """;

        List<Clausula> lista = new ArrayList<>();
        String filtro = "%" + termo + "%";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filtro);
            ps.setString(2, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao pesquisar cláusulas: " + e.getMessage());
        }

        return lista;
    }

    // ==================== PRÓXIMO NÚMERO ====================
    /**
     * Retorna o próximo número sequencial de cláusula para um contrato.
     * Ex: se o contrato já tem cláusulas 1, 2, 3 → retorna 4.
     */
    public int proximoNumero(int contratoId) {
        String sql = "SELECT COALESCE(MAX(numero), 0) + 1 FROM clausulas WHERE contrato_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contratoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao calcular próximo número: " + e.getMessage());
        }

        return 1;
    }

    // ==================== VERIFICAR NÚMERO DUPLICADO ====================
    /**
     * Verifica se já existe uma cláusula com o mesmo número dentro do mesmo contrato.
     */
    public boolean numeroDuplicado(int contratoId, int numero, int idIgnorar) {
        String sql = """
            SELECT COUNT(*) FROM clausulas
            WHERE contrato_id = ? AND numero = ? AND id != ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contratoId);
            ps.setInt(2, numero);
            ps.setInt(3, idIgnorar);

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

    // ==================== MAPEAMENTO ====================
    private Clausula mapear(ResultSet rs) throws SQLException {
        Clausula c = new Clausula();
        c.setId(rs.getInt("id"));
        c.setContratoId(rs.getInt("contrato_id"));
        c.setNumero(rs.getInt("numero"));
        c.setDescricao(rs.getString("descricao"));

        Timestamp criadoEm = rs.getTimestamp("criado_em");
        c.setCriadoEm(criadoEm != null ? criadoEm.toLocalDateTime() : null);

        c.setNumeroContrato(rs.getString("numero_contrato"));

        return c;
    }
}
