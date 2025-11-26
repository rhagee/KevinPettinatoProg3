package com.client.models.EmailManagement;

import com.client.models.AlertManagement.AlertType;
import communication.Mail;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;

import java.util.ArrayList;

public class EmailItem {
    private final StringProperty sender = new SimpleStringProperty("");
    private final StringProperty subject = new SimpleStringProperty("");
    private final ListProperty<String> receiverList = new SimpleListProperty<String>();
    private final StringProperty message = new SimpleStringProperty("");
    private final Mail mail;

    public EmailItem(Mail mail) {
        this.mail = mail;
        this.sender.set(mail.getSender());
        this.subject.set(mail.getSubject());
        this.message.set(mail.getMessage());
        this.receiverList.setAll(mail.getReceiverList());
    }

    public Mail getMail() {
        return mail;
    }

    public StringProperty senderProperty() {
        return sender;
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public ListProperty<String> receiverListProperty() {
        return receiverList;
    }


}
