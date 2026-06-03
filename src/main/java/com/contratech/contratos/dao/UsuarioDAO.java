package com.contratech.contratos.dao;

import com.contratech.contratos.config.ConnectionFactory;
import com.contratech.contratos.model.Usuario;
import com.contratech.contratos.model.Usuario.TipoUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por todas as operações de banco da entidade Usuario.
 * 
 * Padrão aplicado: cada método abre e fecha sua própria conexão.
 * Em sistema mais complexo usaríamos pool de conexões (HikariCP),
 * mas para desktop com poucos usuários simultâneos, isso é suficiente.
 */
public class UsuarioDAO {

    /**
     * Autentica um usuário pelo email e senha (hash SHA-256).
     * Usado na tela de login.
     *
     * @param email email informado
     * @param senhaHash senha já convertida em SHA-256
     * @return Usuario encontrado ou null se credenciais inválidas
     */
    public Usuario autenticar(String email, String senhaHash) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ? AND ativo = TRUE";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senhaHash);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao autenticar: " + e.getMessage());
        }

        return null; // Credenciais inválidas
    }

    /**
     * Insere um novo usuário no banco.
     * A senha já deve vir como hash SHA-256.
     *
     * @param usuario objeto com os dados a inserir
     * @return true se inseriu com sucesso
     */
    public boolean inserir(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, sobrenome, email, senha, tipo_usuario) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSobrenome());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());
            stmt.setString(5, usuario.getTipoUsuario().name());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao inserir: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza os dados de um usuário existente.
     * Se a senha for null ou vazia, mantém a senha atual (não sobrescreve).
     *
     * @param usuario objeto com os dados atualizados (id obrigatório)
     * @return true se atualizou com sucesso
     */
    public boolean alterar(Usuario usuario) {
        // SQL dinâmico: só atualiza senha se foi informada
        boolean atualizarSenha = usuario.getSenha() != null && !usuario.getSenha().isEmpty();

        String sql;
        if (atualizarSenha) {
            sql = "UPDATE usuarios SET nome = ?, sobrenome = ?, email = ?, senha = ?, "
                + "tipo_usuario = ? WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET nome = ?, sobrenome = ?, email = ?, "
                + "tipo_usuario = ? WHERE id = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSobrenome());
            stmt.setString(3, usuario.getEmail());

            if (atualizarSenha) {
                stmt.setString(4, usuario.getSenha());
                stmt.setString(5, usuario.getTipoUsuario().name());
                stmt.setInt(6, usuario.getId());
            } else {
                stmt.setString(4, usuario.getTipoUsuario().name());
                stmt.setInt(5, usuario.getId());
            }

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao alterar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exclusão lógica: marca o usuário como inativo.
     * O registro permanece no banco para auditoria.
     *
     * @param id identificador do usuário
     * @return true se desativou com sucesso
     */
    public boolean excluir(int id) {
        String sql = "UPDATE usuarios SET ativo = FALSE WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao excluir: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos os usuários ativos.
     * Usado para popular a TableView na tela de usuários.
     *
     * @return lista de usuários ativos
     */
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios WHERE ativo = TRUE ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao listar: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Pesquisa usuários por nome (busca parcial, case-insensitive).
     *
     * @param nome texto de busca
     * @return lista de usuários que contêm o texto no nome
     */
    public List<Usuario> pesquisarPorNome(String nome) {
        String sql = "SELECT * FROM usuarios WHERE LOWER(nome) LIKE ? AND ativo = TRUE ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // O % antes e depois permite busca parcial: "and" encontra "Anderson"
            stmt.setString(1, "%" + nome.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao pesquisar: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Verifica se já existe um usuário com o email informado.
     * Usado para validação antes de inserir/alterar.
     *
     * @param email email a verificar
     * @param idExcluir id a ignorar na busca (para edição do próprio usuário)
     * @return true se o email já está em uso por outro usuário
     */
    public boolean emailJaExiste(String email, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ? AND id != ? AND ativo = TRUE";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, idExcluir);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Erro ao verificar email: " + e.getMessage());
        }

        return false;
    }

    /**
     * Converte uma linha do ResultSet em objeto Usuario.
     * Método privado reutilizado por todos os métodos de consulta.
     * Centralizar isso evita duplicação e garante consistência.
     */
    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setSobrenome(rs.getString("sobrenome"));
        u.setEmail(rs.getString("email"));
        u.setSenha(rs.getString("senha"));
        u.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        u.setAtivo(rs.getBoolean("ativo"));
        u.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return u;
    }
}

