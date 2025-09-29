package com.prog.controllers.email;

import com.prog.models.EmailManagement.EmailItem;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class EmailItemController {

    @FXML private HBox root;
    @FXML private Label title, message;

    private EmailItem item;

    public void bind(EmailItem item)
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
