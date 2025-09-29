package com.prog.controllers.pages;
import com.prog.models.AlertManagement.AlertManager;
import com.prog.models.AlertManagement.AlertType;
import com.prog.models.SceneManagement.SceneNames;
import com.prog.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;


public class HomeController {
    @FXML
    public void OnLogout()
    {
        SceneTransitions.SlideRight(SceneNames.LOGIN);
        AlertManager.get().add("Arrivederci","Logout avvenuto con successo!", AlertType.ERROR);
    }
}
