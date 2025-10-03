package com.server.models;

import com.server.models.threads.SocketHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

public class ProgApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger("INIT");

    private SocketHandler socketHandler;
    private Thread handlerThread;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 900);
        stage.setTitle("Server Mail");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        socketHandler = new SocketHandler();
        handlerThread = new Thread(socketHandler);
        handlerThread.start();

        //LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);

    }

    @Override
    public void stop() {
        handlerThread.interrupt();
    }
}
