package com.server.controllers;

import com.server.models.LogHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.logging.Logger;

public class UIController {
    @FXML
    private TextFlow console;

    @FXML
    private ScrollPane scrollPane;

    private LogHandler handler;

    private boolean autoScroll = true;

    @FXML
    private void initialize() {
        handler = new LogHandler();
        var logger = Logger.getLogger("");
        logger.addHandler(handler);


        handler.getConsoleTextProperty().addListener((observable, oldValue, newValue) -> {
            Text text = new Text(newValue);
            text.setFill(Color.WHITE);
            console.getChildren().add(text);
        });


        console.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (autoScroll) {
                Platform.runLater(() -> {
                    scrollPane.setVvalue(1.0);
                });
            }
        });
    }

    private int n = 0;

    @FXML
    private void RandomPrint() {
        handler.AddManually("Ciao " + n + "\n");
        n++;
    }

}
