package com.client.controllers.pages;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.BackendManagement.BackendManager;
import com.client.models.SceneManagement.SceneNames;
import com.client.models.SceneManagement.SceneTransitions;
import communication.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import utils.RequestCodes;
import utils.ResponseCodes;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LoginController {


    @FXML
    private VBox root;

    @FXML
    private TextField email;

    @FXML
    public void OnLogin() {
        CompletableFuture<Response<?>> onCompleteFuture = new CompletableFuture<>();
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.AUTH, email.getText(), onCompleteFuture);
        onCompleteFuture.thenAccept(onAuthCompleteHandler);
    }

    private final Consumer<Response<?>> onAuthCompleteHandler = response -> {

        if (response.getCode() == ResponseCodes.DISCONNECTED) {
            return;
        }

        if (response.getCode() != ResponseCodes.OK) {
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Autenticazione fallita.", AlertType.ERROR);
            });
            return;
        }

        if (!(response.getPayload() instanceof String token)) {
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Autenticazione fallita.", AlertType.ERROR);
            });
            return;
        }

        BackendManager.INSTANCE.setToken(token);
        Platform.runLater(() -> {
            SceneTransitions.SlideLeft(SceneNames.HOME);
        });
    };


    @FXML
    private void RootPaneClick() {
        root.requestFocus();
    }
}
