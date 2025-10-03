package com.server.models;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.ConnectionInfo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger("SETUP");


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 900);
        stage.setTitle("Server Mail");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        Platform.runLater(() -> {
            LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);
            LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);
            LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);
            LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);
            LOGGER.log(Level.INFO, "LAST");
        });

    }
}
