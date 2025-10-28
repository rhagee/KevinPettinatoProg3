package com.client.controllers.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class SideMenu extends Pane {

    @FXML
    private Label mail;

    public SideMenu() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/prog/ui/components/side_menu.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {

    }

    public void InitMail(String input) {
        System.out.println("SetMail " + input);
        mail.setText(input);
    }

}
