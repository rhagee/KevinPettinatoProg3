module com.kevin_pettinato.prog3.socketserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.server to javafx.fxml;
    exports com.server;
}