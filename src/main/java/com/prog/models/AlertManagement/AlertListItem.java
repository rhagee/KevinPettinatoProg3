package com.prog.models.AlertManagement;

import com.prog.controllers.AlertItemController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class AlertListItem extends ListCell<AlertItem> {
    private final Node rootNode;
    {
        setScaleY(-1); // flip cell back
    }
    private AlertItemController controller;

    public AlertListItem() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/prog/ui/alertItem.fxml"));
            rootNode = fxml.load();
            controller = fxml.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(AlertItem item, boolean empty) {
        super.updateItem(item, empty);
        if(empty || item == null)
        {
            setGraphic(null);
            return;
        }

        controller.bind(item);
        setText(null);
        setGraphic(rootNode);
    }

}
