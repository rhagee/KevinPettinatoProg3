package com.prog.controllers.pages;
import com.prog.models.AlertManagement.AlertManager;
import com.prog.models.AlertManagement.AlertType;
import com.prog.models.SceneManagement.SceneNames;
import com.prog.models.SceneManagement.SceneTransitions;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.util.Duration;


public class HomeController {
    @FXML
    public void OnLogout()
    {
        SceneTransitions.SlideRight(SceneNames.LOGIN);
        AlertManager.get().add("Arrivederci","Logout avvenuto con successo!", AlertType.ERROR);
    }
}
