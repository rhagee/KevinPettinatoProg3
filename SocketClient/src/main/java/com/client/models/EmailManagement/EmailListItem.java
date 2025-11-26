package com.client.models.EmailManagement;

import com.client.controllers.email.EmailItemController;
import communication.Mail;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class EmailListItem extends ListCell<Mail> {
    private final Node rootNode;
    private EmailItemController controller;

    public EmailListItem() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/prog/ui/email/email_item.fxml"));
            rootNode = fxml.load();
            controller = fxml.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Mail item, boolean empty) {
        Platform.runLater(() -> {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            controller.bind(item);
            setText(null);
            setGraphic(rootNode);
        });
    }

}
