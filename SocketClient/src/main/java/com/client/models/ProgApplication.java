package com.client.models;

import com.client.models.SceneManagement.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ProgApplication extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage).start();
    }

}
