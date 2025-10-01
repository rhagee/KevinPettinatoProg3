package com.client.controllers.email;

import com.client.models.EmailManagement.EmailItem;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class EmailListController {
    @FXML
    private ListView<EmailItem> emailList;

    @FXML
    private void initialize() {
        //Take it from EmailManager

        //emailList.setItems(AlertManager.get().getItems());
        //emailList.setCellFactory(_ -> new EmailListItem());
    }

}

