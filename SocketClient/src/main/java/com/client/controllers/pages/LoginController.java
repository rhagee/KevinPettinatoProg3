package com.client.controllers.pages;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.SceneManagement.SceneNames;
import com.client.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    public void OnLogin() {
        SceneTransitions.SlideLeft(SceneNames.HOME);
        AlertManager.get().add("Benvenuto", "Login avvenuto con successo!", AlertType.SUCCESS);
        AlertManager.get().OnConnectionFailed();
    }
}
