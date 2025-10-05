package com.server.models;

import com.server.models.threads.SocketHandler;
import communication.ChunkRange;
import communication.MailBox;
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

        DatabaseHandler.INSTANCE.Initialize();

        //LOGGER.log(Level.INFO, "Server will listen on " + ConnectionInfo.SERVER_IP + ":" + ConnectionInfo.SERVER_PORT);
        /*MailBox mailBox = new MailBox();
        LinkedHashMap<UUID, Integer> temp = new LinkedHashMap<UUID, Integer>();


        temp.putFirst(UUID.randomUUID(), 25);
        temp.putFirst(UUID.randomUUID(), 10);
        temp.putFirst(UUID.randomUUID(), 4);
        temp.putFirst(UUID.randomUUID(), 20);
        temp.putFirst(UUID.randomUUID(), 6);

        mailBox.TempReceivedSetter(temp);

        LOGGER.info("MAP");
        temp.forEach((s, integer) -> {
            LOGGER.info("UUID : " + s + " - Value " + integer);
        });

        LOGGER.info("EVALUATION");
        var ranges = mailBox.getReceivedChunks(5, 0);
        for (ChunkRange range : ranges) {
            LOGGER.info(" Chunk(" + range.getId() + " start: " + range.getStart() + ", end : " + range.getEnd() + ")");
        }

        String jsonString = new ObjectMapper().writeValueAsString(mailBox);
        MailBox deserialized = new ObjectMapper().readValue(jsonString, MailBox.class);
        LOGGER.info("DESERIALIZED MAP");
        deserialized.getReceivedBucket().forEach((s, integer) -> {
            LOGGER.info("UUID : " + s + " - Value " + integer);
        });
        LOGGER.info(jsonString);*/

    }

    @Override
    public void stop() {
        handlerThread.interrupt();
    }
}
