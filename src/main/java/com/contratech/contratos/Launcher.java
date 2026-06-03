package com.contratech.contratos;

/**
 * Ponto de entrada para execução via fat JAR.
 * 
 * O JavaFX impede que a classe main de um JAR executável
 * estenda Application diretamente. Este launcher resolve isso
 * delegando para a classe App (que estende Application).
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}

