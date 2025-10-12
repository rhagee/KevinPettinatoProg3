package com.server.controllers;

import com.server.models.DatabaseHandler;
import com.server.models.LogHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import utils.MailUtility;

import java.util.List;
import java.util.logging.Logger;

public class UIController {

    private final String MAIL_LABEL_STYLE = "-fx-text-fill: white;";

    @FXML
    private TextFlow console;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox mailListBox;

    private LogHandler handler;

    private boolean autoScroll = true;

    @FXML
    private TextField newMailText;

    @FXML
    private Label errorText;

    @FXML
    private void initialize() {
        HideErrorText();
        InitializeConsole();
        InitializeMailHandler();
    }

    private void InitializeConsole() {

        handler = new LogHandler();
        var logger = Logger.getLogger("");
        logger.addHandler(handler);

        scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
            Platform.runLater(() -> {
                autoScroll = scrollPane.getVvalue() >= 0.999;
            });
        });

        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            Platform.runLater(() -> {
                autoScroll = scrollPane.getVvalue() >= 0.999;
            });
        });

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.999) {
                autoScroll = true;
            }
        });

        handler.getConsoleTextProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                Text text = new Text(newValue);
                text.setFill(Color.WHITE);
                console.getChildren().add(text);
            });
        });


        console.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (autoScroll) {
                Platform.runLater(() -> {
                    scrollPane.setVvalue(1.0);
                });
            }
        });
    }

    private void InitializeMailHandler() {

        DatabaseHandler.INSTANCE.addAccountsListener((observable, oldValue, newValue) -> {
            mailListBox.getChildren().clear();
            for (String mail : newValue) {
                Label label = new Label(mail);
                label.setStyle(MAIL_LABEL_STYLE);
                mailListBox.getChildren().add(label);
            }
        });
    }

    @FXML
    private void onAdd() {
        HideErrorText();

        String newMail = newMailText.getText();

        if (!CheckMail(newMail)) {
            return;
        }

        if (DatabaseHandler.INSTANCE.addUser(newMail)) {
            newMailText.clear();
        } else {
            ShowErrorText("Mail gia presente!");
        }
    }

    private void HideErrorText() {
        errorText.setVisible(false);
    }

    private void ShowErrorText(String error) {
        errorText.setText(error);
        errorText.setVisible(true);
    }

    private boolean CheckMail(String mail) {

        if (mail.isBlank()) {
            return false;
        }

        if (!MailUtility.validate(mail)) {
            ShowErrorText("Mail non valida.\nEsempio: kevin@unito.it");
            return false;
        }

        return true;
    }
}
