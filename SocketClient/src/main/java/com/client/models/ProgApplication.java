package com.client.models;

import com.client.models.BackendManagement.BackendManager;
import com.client.models.SceneManagement.SceneManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ProgApplication extends Application {

    String[] fonts = {
            "Poppins-Black.ttf",
            "Poppins-BlackItalic.ttf",
            "Poppins-Bold.ttf",
            "Poppins-BoldItalic.ttf",
            "Poppins-ExtraBold.ttf",
            "Poppins-ExtraBoldItalic.ttf",
            "Poppins-ExtraLight.ttf",
            "Poppins-ExtraLightItalic.ttf",
            "Poppins-Italic.ttf",
            "Poppins-Light.ttf",
            "Poppins-LightItalic.ttf",
            "Poppins-Medium.ttf",
            "Poppins-MediumItalic.ttf",
            "Poppins-Regular.ttf",
            "Poppins-SemiBold.ttf",
            "Poppins-SemiBoldItalic.ttf",
            "Poppins-Thin.ttf",
            "Poppins-ThinItalic.ttf"
    };

    @Override
    public void start(Stage stage) {
        for (String f : fonts) {
            Font font = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/" + f), 12
            );
        }

        SceneManager.init(stage).start();
    }

    @Override
    public void stop() {
        System.out.println("Gracefully stopping threads and closing connection");
        SceneManager.get().StopBackendThread();
        BackendManager.INSTANCE.ClearConnection();
    }
}
