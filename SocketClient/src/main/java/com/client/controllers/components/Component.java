package com.client.controllers.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class Component extends StackPane {

    protected void initializeComponent(String resource) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(resource));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
