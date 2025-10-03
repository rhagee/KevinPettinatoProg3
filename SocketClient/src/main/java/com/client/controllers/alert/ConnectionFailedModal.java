package com.client.controllers.alert;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.SceneManagement.SceneManager;
import com.client.models.SceneManagement.SceneNames;
import com.client.models.SceneManagement.SceneTransitions;
import javafx.application.Platform;
import javafx.fxml.FXML;

public class ConnectionFailedModal {

    @FXML
    public void onClose() {
        Platform.exit();
    }

    @FXML
    public void onRetry() {
        SceneManager.get().TryConnectToBackend();
    }
}
