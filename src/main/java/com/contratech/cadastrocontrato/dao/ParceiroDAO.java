package com.contratech.cadastrocontrato.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.contratech.cadastrocontrato.connection.ConnectionFactory;
import com.contratech.cadastrocontrato.model.Parceiro;

/**
 * DAO responsável por todas as operações de banco da entidade Parceiro.
 */
public class ParceiroDAO {

    /**
     * Verifica se já existe um parceiro ativo com o mesmo documento (CPF/CNPJ).
     * Exclui da verificação o próprio parceiro sendo editado (pelo ID).
     *
     * @param documento CPF ou CNPJ (somente números)
     * @param idExcluir ID a ignorar (0 para inserção)
     * @return true se o documento já está em uso por outro parceiro
     */
    public boolean documentoJaExiste(String documento, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM parceiros "
                + "WHERE documento = ? AND ativo = TRUE AND id != ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, documento);
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO] Verificação documento duplicado: " + e.getMessage());
        }

        return false;
    }

    public boolean inserir(Parceiro parceiro) {
        String sql = "INSERT INTO parceiros (razao_social, cnpj_cpf, endereco, cidade, uf, cep, telefone, email) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parceiro.getRazaoSocial());
            stmt.setString(2, parceiro.getCnpjCpf());
            stmt.setString(3, parceiro.getEndereco());
            stmt.setString(4, parceiro.getCidade());
            stmt.setString(5, parceiro.getUf());
            stmt.setString(6, parceiro.getCep());
            stmt.setString(7, parceiro.getTelefone());
            stmt.setString(8, parceiro.getEmail());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao inserir: " + e.getMessage());
            return false;
        }
    }

    public boolean alterar(Parceiro parceiro) {
        String sql = "UPDATE parceiros SET razao_social = ?, cnpj_cpf = ?, endereco = ?, "
                + "cidade = ?, uf = ?, cep = ?, telefone = ?, email = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parceiro.getRazaoSocial());
            stmt.setString(2, parceiro.getCnpjCpf());
            stmt.setString(3, parceiro.getEndereco());
            stmt.setString(4, parceiro.getCidade());
            stmt.setString(5, parceiro.getUf());
            stmt.setString(6, parceiro.getCep());
            stmt.setString(7, parceiro.getTelefone());
            stmt.setString(8, parceiro.getEmail());
            stmt.setInt(9, parceiro.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao alterar: " + e.getMessage());
            return false;
        }
    }

    public boolean excluir(int id) {
        String sql = "UPDATE parceiros SET ativo = FALSE WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao excluir: " + e.getMessage());
            return false;
        }
    }

    public List<Parceiro> listarTodos() {
        String sql = "SELECT * FROM parceiros WHERE ativo = TRUE ORDER BY razao_social";
        List<Parceiro> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao listar: " + e.getMessage());
        }

        return lista;
    }

    public boolean cnpjJaExiste(String cnpj, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM parceiros WHERE cnpj_cpf = ? AND id != ?";
        try (Connection conn = ConexaoDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            stmt.setInt(2, idIgnorar);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Parceiro> pesquisarPorNome(String razaoSocial) {
        String sql = "SELECT * FROM parceiros WHERE LOWER(razao_social) LIKE ? AND ativo = TRUE ORDER BY razao_social";
        List<Parceiro> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + razaoSocial.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao pesquisar por nome: " + e.getMessage());
        }

        return lista;
    }

    public List<Parceiro> pesquisarPorCnpj(String cnpjCpf) {
        String sql = "SELECT * FROM parceiros WHERE cnpj_cpf LIKE ? AND ativo = TRUE ORDER BY razao_social";
        List<Parceiro> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + cnpjCpf + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ParceiroDAO] Erro ao pesquisar por CNPJ: " + e.getMessage());
        }

        return lista;
    }

    private Parceiro mapearResultSet(ResultSet rs) throws SQLException {
        Parceiro p = new Parceiro();
        p.setId(rs.getInt("id"));
        p.setRazaoSocial(rs.getString("razao_social"));
        p.setCnpjCpf(rs.getString("cnpj_cpf"));
        p.setEndereco(rs.getString("endereco"));
        p.setCidade(rs.getString("cidade"));
        p.setUf(rs.getString("uf"));
        p.setCep(rs.getString("cep"));
        p.setTelefone(rs.getString("telefone"));
        p.setEmail(rs.getString("email"));
        p.setAtivo(rs.getBoolean("ativo"));

        // null-check obrigatório: criado_em pode ser null em registros antigos
        java.sql.Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) {
            p.setCriadoEm(ts.toLocalDateTime());
        }

        return p;
    }
}
