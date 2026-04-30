package com.contratech.cadastrocontrato.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões com o banco PostgreSQL.
 * 
 * Centraliza a criação de conexões em um único ponto.
 * Se amanhã mudar o banco, a senha ou o host, você altera AQUI e só aqui.
 * 
 * IMPORTANTE: Em produção, essas credenciais viriam de variáveis de ambiente
 * ou de um arquivo de configuração externo — NUNCA hardcoded no código.
 * Para fins de aprendizado, vamos manter assim por enquanto.
 */
public class ConnectionFactory {

    // Configurações do banco
    private static final String URL = "jdbc:postgresql://localhost:5432/cadastro_contrato";
    private static final String USUARIO = "postgres";
    private static final String SENHA = "senac2025"; // Troque pela sua senha do PostgreSQL

    /**
     * Cria e retorna uma nova conexão com o banco de dados.
     * 
     * @return Connection ativa com o PostgreSQL
     * @throws SQLException se não conseguir conectar
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver do PostgreSQL explicitamente
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("[DB] Conexão estabelecida com sucesso.");
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado. Verifique o pom.xml.", e);

        } catch (SQLException e) {
            System.err.println("[DB] ERRO ao conectar: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Fecha a conexão de forma segura.
     * 
     * @param conn conexão a ser fechada
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[DB] Conexão fechada.");
            } catch (SQLException e) {
                System.err.println("[DB] ERRO ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
