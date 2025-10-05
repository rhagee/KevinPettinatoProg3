package com.client.controllers.alert;

import com.client.models.AlertManagement.AlertType;
import com.client.models.BackendManagement.BackendManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ConnectionRetryModal {

    @FXML
    private Label retryAttemptsText;

    @FXML
    private void initialize() {

        BackendManager.INSTANCE.getRetryAttempts().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                retryAttemptsText.setText("Tentativo " + newVal + "/" + BackendManager.MAX_RETRY);
            });
        });
    }

}
