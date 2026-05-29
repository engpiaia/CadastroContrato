module com.contratech.cadastrocontrato {
    // Módulos JavaFX necessários
    requires javafx.controls;
    requires javafx.fxml;

    // conexão com API
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Driver JDBC do PostgreSQL
    requires java.sql;

    // Abre o pacote de views pro JavaFX renderizar os componentes
    opens com.contratech.cadastrocontrato to javafx.graphics;
    opens com.contratech.cadastrocontrato.view to javafx.fxml;

    // Exporta os pacotes para uso externo (necessário para o JavaFX acessar)
    exports com.contratech.cadastrocontrato;
    exports com.contratech.cadastrocontrato.view;
    exports com.contratech.cadastrocontrato.model;
    exports com.contratech.cadastrocontrato.dao;
    exports com.contratech.cadastrocontrato.connection;
    exports com.contratech.cadastrocontrato.util;
}
