package com.prog.controllers.pages;
import com.prog.models.AlertManagement.AlertManager;
import com.prog.models.AlertManagement.AlertType;
import com.prog.models.SceneManagement.SceneNames;
import com.prog.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;
public class LoginController {

    @FXML
    public void OnLogin()
    {
        SceneTransitions.SlideLeft(SceneNames.HOME);
        AlertManager.get().add("Benvenuto","Login avvenuto con successo!", AlertType.SUCCESS);
    }
}
