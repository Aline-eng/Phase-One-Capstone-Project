module com.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;  // needed for JDBC in Lab 2 & 3

    // Opens our packages to javafx.fxml so it can use reflection to load controllers
    opens com.igirepay to javafx.fxml;
    opens com.igirepay.lab3_mini_capstone to javafx.fxml;

    exports com.igirepay;
    exports com.igirepay.lab1_oop;
    exports com.igirepay.lab1_oop.util;
    exports com.igirepay.lab2_jdbc;
    exports com.igirepay.lab2_jdbc.db;
    exports com.igirepay.lab2_jdbc.dao;
    exports com.igirepay.lab3_mini_capstone;
}
