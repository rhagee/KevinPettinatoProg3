package com.prog.controllers.email;

import com.prog.models.EmailManagement.EmailItem;
import com.prog.models.EmailManagement.EmailListItem;
import com.prog.models.AlertManagement.AlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class EmailListController {
    @FXML
    private ListView<EmailItem> emailList;

    @FXML
    private void initialize()
    {
        //Take it from EmailManager

        //emailList.setItems(AlertManager.get().getItems());
        //emailList.setCellFactory(_ -> new EmailListItem());
    }

}

