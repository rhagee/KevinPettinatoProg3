package com.client.controllers.pages;

import com.client.models.EmailManagement.MailBoxManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.*;

public class LoginController {


    @FXML
    private VBox root;

    @FXML
    private TextField email;

    @FXML
    public void OnLogin() {
        MailBoxManager.INSTANCE.onLogin(email.getText());
    }

    @FXML
    private void RootPaneClick() {
        root.requestFocus();
    }
}
