package com.contratech.contratos.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitário para hash de senhas.
 * 
 * NOTA: SHA-256 é suficiente para este projeto didático.
 * Em produção comercial real, use BCrypt ou Argon2 — são resistentes
 * a ataques de força bruta por serem propositalmente lentos.
 */
public class SenhaUtil {

    /**
     * Gera o hash SHA-256 de uma string.
     *
     * @param senha texto puro
     * @return hash em hexadecimal (64 caracteres)
     */
    public static String hashSHA256(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));

            // Converte bytes em string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível na JVM", e);
        }
    }
}
