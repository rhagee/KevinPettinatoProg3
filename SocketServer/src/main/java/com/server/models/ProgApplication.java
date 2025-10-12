package com.server.models;

import com.server.models.threads.SocketHandler;
import communication.ChunkRange;
import communication.MailBox;
import communication.SmallMail;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProgApplication extends Application {
    public static boolean APPLICATION_CLOSED = false;

    private static final Logger LOGGER = Logger.getLogger("INIT");

    private SocketHandler socketHandler;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 900);
        stage.setTitle("Server Mail");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        DatabaseHandler.INSTANCE.Initialize();

        socketHandler = new SocketHandler();
        socketHandler.start();


        //TEST
        SmallMail mail = new SmallMail();
        mail.CreateEmpty("test@test.com");
        mail.addReceiver("kevin@kevin.com");
        mail.setSubject("Invio Mail Test");
        mail.setMessage("Ciao questa è una mail di prova!");

        DatabaseHandler.INSTANCE.sendMail(mail);
        //ENDTEST

    }

    @Override
    public void stop() {
        APPLICATION_CLOSED = true;
        socketHandler.interrupt();
    }
}
