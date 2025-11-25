package com.client.controllers.email;

import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.EmailManagement.PageStatus;
import communication.Mail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;

public class EmailItemController {

    @FXML
    private HBox root;
    @FXML
    private Label userMail, subject, message, dateTime;

    private Mail mail;

    private final String TO_READ_STYLE = "row-container";
    private final String READ_STYLE = "row-container-read";

    public void bind(Mail newMail) {
        if (this.mail != null) {
            userMail.textProperty().unbind();
            subject.textProperty().unbind();
            root.styleProperty().unbind();
            message.textProperty().unbind();
            dateTime.textProperty().unbind();
        }

        setUserMail(newMail);
        setEllipsesString(subject, newMail.getSubject(), 25);
        setEllipsesString(message, newMail.getMessage(), 50);
        setDate(newMail);

        if (newMail.getReadProp() == null) {
            newMail.syncReadProp();
        }

        SetReadUnreadStyle(newMail.getRead());
        newMail.getReadProp().addListener((observable, oldValue, newValue) -> {
            SetReadUnreadStyle(newValue);
        });

        mail = newMail;
    }

    private void setDate(Mail newMail) {
        try {
            LocalDateTime date = newMail.getDateTime();
            if (date != null) {
                String year = date.getYear() + "";
                String month = date.getMonthValue() + "";
                String day = date.getDayOfMonth() + "";
                String hour = String.format("%02d", date.getHour());
                String minute = String.format("%02d", date.getMinute());
                String formattedDate = day + "/" + month + "/" + year + " (" + hour + ":" + minute + ")";
                dateTime.setText(formattedDate);
            } else {
                dateTime.setText("N/D");
            }
        } catch (Exception e) {
            dateTime.setText("N/D");
            e.printStackTrace();
        }

    }


    private void setUserMail(Mail newMail) {
        String userMailText = "";
        switch (MailBoxManager.INSTANCE.statusProperty().getValue()) {
            case PageStatus.SENT:
                userMailText = "A: " + String.join(", ", newMail.getReceiverList());
                break;
            case PageStatus.RECEIVED:
                userMailText = newMail.getSender();
            default:
                break;
        }

        setEllipsesString(userMail, userMailText, 25);
    }

    private void setEllipsesString(Label target, String text, int maxChar) {
        String finalText = text;

        if (text.length() > maxChar) {
            finalText = text.substring(0, maxChar);
            finalText += "...";
        }

        target.setText(finalText);
    }

    private void SetReadUnreadStyle(boolean read) {
        boolean isSentPage = MailBoxManager.INSTANCE.statusProperty().getValue() == PageStatus.SENT;
        Platform.runLater(() -> {
            if (isSentPage || read) {
                root.getStyleClass().remove(TO_READ_STYLE);
                root.getStyleClass().add(READ_STYLE);
            } else {
                root.getStyleClass().remove(READ_STYLE);
                root.getStyleClass().add(TO_READ_STYLE);
            }
        });
    }

    @FXML
    private void onClick() {
        MailBoxManager.INSTANCE.openMailDrawer(this.mail);
    }
}
