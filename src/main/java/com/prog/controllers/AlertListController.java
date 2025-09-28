package com.prog.controllers;

import com.prog.models.AlertManagement.AlertItem;
import com.prog.models.AlertManagement.AlertListItem;
import com.prog.models.AlertManagement.AlertManager;
import com.prog.models.AlertManagement.AlertType;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class AlertListController {
    @FXML
    private ListView<AlertItem> alertList;

    @FXML
    private void initialize()
    {
        alertList.setItems(AlertManager.get().getItems());
        alertList.setCellFactory(_ -> new AlertListItem());
    }

}

