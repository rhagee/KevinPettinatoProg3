module socketMailer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.prog to javafx.fxml;
    exports com.prog;
    exports com.prog.controllers;
    opens com.prog.controllers to javafx.fxml;
    exports com.prog.models;
    opens com.prog.models to javafx.fxml;
}