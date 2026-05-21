package com.contratech.cadastrocontrato.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.contratech.cadastrocontrato.model.Usuario;

/**
 * Utilitário simples para gravação de eventos de auditoria relacionados a acesso.
 * Escreve linhas no arquivo `logs/access_audit.log` no diretório do projeto.
 */
public class AuditUtil {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("access_audit.log");
    private static final DateTimeFormatter TF = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static synchronized void logDeniedAccess(Usuario u, String action, String reason) {
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }

            String userPart = (u == null) ? "anonymous" : u.getNome() + " (id=" + u.getId() + ")";
            String rolePart = (u == null) ? "-" : String.valueOf(u.getTipoUsuario());
            String ts = TF.format(ZonedDateTime.now());
            String line = String.format("%s | DENIED | user=%s | role=%s | action=%s | reason=%s%n",
                    ts, userPart, rolePart, action, reason);

            Files.write(LOG_FILE, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException ex) {
            // Não interrompe a aplicação por falha em gravar log; em produção reimplementar handling
            System.err.println("Falha ao gravar log de auditoria: " + ex.getMessage());
        }
    }
}
