package com.client.controllers.email;

import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.EmailManagement.PageStatus;
import communication.Mail;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class EmailItemController {

    @FXML
    private HBox root;
    @FXML
    private Label userMail, subject, message;

    private Mail mail;

    public void bind(Mail newMail) {
        if (this.mail != null) {
            userMail.textProperty().unbind();
            subject.textProperty().unbind();
            root.styleProperty().unbind();
            message.textProperty().unbind();
        }

        setUserMail(newMail);
        setEllipsesString(subject, newMail.getSubject(), 25);
        setEllipsesString(message, newMail.getMessage(), 50);

        mail = newMail;
    }


    private void setUserMail(Mail newMail) {
        String userMailText = "";
        switch (MailBoxManager.INSTANCE.statusProperty().getValue()) {
            case PageStatus.SENT:
                userMailText = String.join(", ", newMail.getReceiverList());
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
}
