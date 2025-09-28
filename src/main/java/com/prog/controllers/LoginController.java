package com.prog.controllers;
import com.prog.models.SceneManagement.SceneNames;
import com.prog.models.SceneManagement.SceneTransitions;
import javafx.fxml.FXML;
public class LoginController {

    @FXML
    public void OnLogin()
    {
        SceneTransitions.SlideLeft(SceneNames.HOME);
    }
}
