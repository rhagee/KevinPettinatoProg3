package com.server.controllers;

import com.server.models.LogHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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

        scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
            Platform.runLater(() -> {
                System.out.println("SCROLL FINISHED");
                autoScroll = scrollPane.getVvalue() >= 0.999;
            });
        });

        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            Platform.runLater(() -> {
                System.out.println("DRAG FINISHED");
                autoScroll = scrollPane.getVvalue() >= 0.999;
            });
        });

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.999) {
                autoScroll = true;
            }
        });

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
