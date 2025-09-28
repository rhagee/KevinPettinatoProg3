package com.prog.models;

import com.prog.models.SceneManagement.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ProgApplication extends Application {

    @Override
    public void start(Stage stage)
    {
        SceneManager.init(stage).start();
    }


}
