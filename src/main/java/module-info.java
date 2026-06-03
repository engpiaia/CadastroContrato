module com.contratech.contratos {
    // Modulos JavaFX necessarios
    requires javafx.controls;
    requires javafx.fxml;

    // conexao com API
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Driver JDBC do PostgreSQL
    requires java.sql;

    // Abre o pacote de views pro JavaFX renderizar os componentes
    opens com.contratech.contratos to javafx.graphics;
    opens com.contratech.contratos.view to javafx.fxml;

    // Exporta os pacotes para uso externo (necessario para o JavaFX acessar)
    exports com.contratech.contratos;
    exports com.contratech.contratos.view;
    exports com.contratech.contratos.model;
    exports com.contratech.contratos.dao;
    exports com.contratech.contratos.config;
    exports com.contratech.contratos.dto;
    exports com.contratech.contratos.util.ui;
    exports com.contratech.contratos.util.security;
}
