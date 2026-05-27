module com.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    // Opens packages to javafx.fxml so it can inject @FXML fields into controllers
    opens com.igirepay to javafx.fxml;
    opens com.igirepay.lab3_mini_capstone to javafx.fxml;
    // controller package will be opened once controllers are added in lab3/auth

    exports com.igirepay;
    exports com.igirepay.lab1_oop;
    exports com.igirepay.lab1_oop.util;
    exports com.igirepay.lab2_jdbc;
    exports com.igirepay.lab2_jdbc.db;
    exports com.igirepay.lab2_jdbc.dao;
    exports com.igirepay.lab2_jdbc.service;
    exports com.igirepay.lab3_mini_capstone;
    exports com.igirepay.lab3_mini_capstone.util;
    // controller export will be added once controllers are added in lab3/auth
}
