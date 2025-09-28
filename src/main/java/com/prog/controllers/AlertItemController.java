package com.prog.controllers;

import com.prog.models.AlertManagement.AlertItem;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class AlertItemController {

    @FXML private HBox root;
    @FXML private Label title, message;

    private AlertItem item;

    public void bind(AlertItem item)
    {
        if(this.item != null)
        {
            title.textProperty().unbind();
            root.styleProperty().unbind();
            message.textProperty().unbind();
        }

        this.item = item;
        title.textProperty().bind(item.titleProperty());
        message.textProperty().bind(item.messageProperty());
        root.styleProperty().bind(
                Bindings.createStringBinding(() -> switch (item.typeProperty().get()) {
                            case ERROR -> "-fx-background-color: #ff5555;";
                            case WARN -> "-fx-background-color: #ffcc00;";
                            case INFO -> "-fx-background-color: #4caf50;";
                            default -> "-fx-background-color: transparent";
                        },
                        item.typeProperty()
                ));
    }
}
