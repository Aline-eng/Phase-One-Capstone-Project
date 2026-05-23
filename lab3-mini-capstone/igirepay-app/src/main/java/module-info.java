module com.igirepay.igirepayapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.igirepay.igirepayapp to javafx.fxml;
    exports com.igirepay.igirepayapp;
}