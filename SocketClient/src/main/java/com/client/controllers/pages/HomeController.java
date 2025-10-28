package com.client.controllers.pages;

import com.client.controllers.components.SideMenu;
import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.SceneManagement.SceneNames;
import com.client.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    SideMenu sideMenu;


    @FXML
    private void initialize() {
        sideMenu.InitMail("Pippo");
    }

    @FXML
    public void OnLogout() {
        SceneTransitions.SlideRight(SceneNames.LOGIN);
        AlertManager.get().add("Arrivederci", "Logout avvenuto con successo!", AlertType.ERROR);
    }


}
