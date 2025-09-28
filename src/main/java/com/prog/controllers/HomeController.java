package com.prog.controllers;
import com.prog.models.SceneManagement.SceneNames;
import com.prog.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;


public class HomeController {
    @FXML
    public void OnLogout()
    {
        SceneTransitions.SlideRight(SceneNames.LOGIN);
    }
}
