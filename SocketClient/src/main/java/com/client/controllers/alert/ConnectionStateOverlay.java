package com.client.controllers.alert;

import com.client.models.BackendManagement.BackendManager;
import com.client.models.BackendManagement.ConnectionState;
import com.client.models.SceneManagement.SceneManager;
import com.client.utils.Colors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;

public class ConnectionStateOverlay {

    @FXML
    private ProgressIndicator loader;

    @FXML
    private Circle statusIcon;

    @FXML
    private Label statusLabel;

    @FXML
    private Button retryBtn;

    private String originalTextStyle;

    @FXML
    private void initialize() {
        originalTextStyle = statusLabel.getStyle();
        var state = BackendManager.INSTANCE.getState();
        ChangeStatus(state.getValue());
        state.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                ChangeStatus(newVal);
            });
        });
    }

    private void ChangeStatus(ConnectionState state) {
        switch (state) {
            case ConnectionState.CONNECTED:
                SetElementVisibility(retryBtn, false);
                SetElementVisibility(loader, false);
                SetGradientColor(Colors.GREEN, Colors.DARKGREEN);
                SetElementVisibility(statusIcon, true);
                statusLabel.setText("Connesso");
                statusLabel.setStyle(originalTextStyle + " -fx-text-fill:" + Colors.DARKGREEN + ";");
                break;
            case ConnectionState.DISCONNECTED:
                SetElementVisibility(loader, false);

                SetElementVisibility(retryBtn, true);
                SetGradientColor(Colors.RED, Colors.DARKRED);
                SetElementVisibility(statusIcon, true);
                statusLabel.setText("Disconnesso");
                statusLabel.setStyle(originalTextStyle + " -fx-text-fill:" + Colors.DARKRED + ";");
                break;
            case ConnectionState.CONNECTING:
                SetElementVisibility(retryBtn, false);
                SetElementVisibility(statusIcon, false);
                SetElementVisibility(loader, true);
                statusLabel.setText("Riconnettendo");
                statusLabel.setStyle(originalTextStyle + " -fx-text-fill:" + Colors.LIGHT_DARK + ";");
                break;
            default:
                System.err.println("Something went wrong : connection State not found!");
                break;
        }
    }

    private void SetElementVisibility(Node node, boolean value) {
        node.setManaged(value);
        node.setVisible(value);
    }

    private void SetGradientColor(Colors color1, Colors color2) {

        Stop[] stops = new Stop[]{
                new Stop(0, Color.web(color1.getValue())),
                new Stop(1, Color.web(color2.getValue()))
        };

        RadialGradient gradient = new RadialGradient(
                0L, 0L, 0.25, 0.25, 0.75, true, CycleMethod.NO_CYCLE, stops
        );
        statusIcon.setFill(gradient);
    }

    @FXML
    public void onRetry() {
        SceneManager.get().TryConnectToBackend();
    }

}
