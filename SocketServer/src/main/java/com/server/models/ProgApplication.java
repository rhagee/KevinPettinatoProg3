package com.server.models;

import com.server.models.threads.SocketHandler;
import communication.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
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
        /*SmallMail mail = new SmallMail();
        mail.CreateEmpty("test@test.com");
        mail.addReceiver("kevin@kevin.com");
        mail.setSubject("Invio Mail Test");
        mail.setMessage("Ciao questa è una mail di prova!");

        DatabaseHandler.INSTANCE.sendMail(mail);*/

        /*int length = 10;
        QueryResult<Integer> resultQuery = DatabaseHandler.INSTANCE.getMailPagesNumber("kevin@kevin.com", length);
        int maxPages = resultQuery.getPayload();
        LOGGER.info("PAGES  " + maxPages);

        MailPageRequest request = new MailPageRequest(0, length);
        QueryResult<List<Mail>> result = DatabaseHandler.INSTANCE.getMailPage("kevin@kevin.com", request);
        List<Mail> mails = result.getPayload();
        for (Mail mail : mails) {
            LOGGER.info(mail.getSubject());
        }*/


        /*String mail = "kevin@kevin.com";
        Mail toDelete = new Mail(UUID.fromString("dfb308a9-499c-41a3-9893-a76265f81ee4"));
        toDelete.setChunkID(UUID.fromString("14e31e83-b7b6-40cf-97c0-c9f5ece723c5"));
        toDelete.setSender("");
        toDelete.addReceiver(mail);
        DatabaseHandler.INSTANCE.deleteMail(mail, toDelete);*/


        /*String mail = "kevin@kevin.com";
        Mail toRead = new Mail(UUID.fromString("cb2870fc-e6a2-43b8-a5ec-57bcf587e35c"));
        toRead.setChunkID(UUID.fromString("a9af3508-7754-42d7-9c28-4bfb8f4a34b3"));
        toRead.setSender("");
        toRead.addReceiver(mail);
        DatabaseHandler.INSTANCE.readUnreadMail(mail, toRead, false);*/

        /*
        "sender": "test@test.com",
      "receiverList": ["kevin@kevin.com"],
      "subject": "Invio Mail Test 2",
      "message": "Ciao questa è una mail di prova!",
      "chunkID": "b53d0dbe-f742-4aad-bb33-779419c9d3e7",
      "id": "fb8d81a8-e943-4160-a0b4-3a29e9c8b2a6"
      */

        //ENDTEST

    }

    @Override
    public void stop() {
        APPLICATION_CLOSED = true;
        socketHandler.interrupt();
    }
}
