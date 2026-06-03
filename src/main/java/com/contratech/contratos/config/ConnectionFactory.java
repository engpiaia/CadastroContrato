package com.contratech.contratos.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fábrica de conexões com o banco PostgreSQL.
 *
 * Centraliza a criação de conexões em um único ponto.
 * As credenciais são lidas de variáveis de ambiente ou de um arquivo
 * de configuração externo (.env), nunca hardcoded no código.
 */
public class ConnectionFactory {

    private static final Map<String, String> ENV = loadEnvFile();

    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT = getEnv("DB_PORT", "5432");
    private static final String DB_NAME = getEnv("DB_NAME", "cadastro_contrato");
    private static final String DB_USER = getEnv("DB_USER", "postgres");
    private static final String DB_PASSWORD = getRequiredEnv("DB_PASSWORD");

    private static final String URL = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    /**
     * Cria e retorna uma nova conexão com o banco de dados.
     *
     * @return Connection ativa com o PostgreSQL
     * @throws SQLException se não conseguir conectar
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
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

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (isBlank(value)) {
            value = ENV.get(key);
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static String getRequiredEnv(String key) {
        String value = getEnv(key, null);
        if (isBlank(value)) {
            throw new IllegalStateException("Variável de ambiente obrigatória ausente: " + key
                    + " (defina em .env ou no ambiente do sistema)");
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> env = new HashMap<>();
        Path envFile = findEnvFile();
        if (envFile == null) {
            return env;
        }

        try {
            List<String> lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                env.put(key, value);
            }
            System.out.println("[DB] Arquivo .env carregado: " + envFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DB] Falha ao ler o arquivo .env: " + e.getMessage());
        }
        return env;
    }

    private static Path findEnvFile() {
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 5 && dir != null; i++) {
            Path candidate = dir.resolve(".env");
            if (Files.exists(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }
}
