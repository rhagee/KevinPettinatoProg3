package com.client.controllers.components;

import com.client.models.EmailManagement.MailBoxManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SideMenu extends Component {
    protected String RESOURCE_NAME = "/com/prog/ui/components/side_menu.fxml";
    private final String DEFAULT_TEXT = "Posta in arrivo";

    private final String BUTTON_CLASS = "menu-voice";
    private final String SELECTED_BUTTON_CLASS = "menu-voice-selected";

    @FXML
    private Label email;

    @FXML
    private Button defaultMenuVoice;

    private Button selectedButton = null;

    public SideMenu() {
        initializeComponent(RESOURCE_NAME);
    }

    @FXML
    private void initialize() {
        initializeBindings();
        selectButtonInternal(defaultMenuVoice, true);
    }

    @FXML
    private void initializeBindings() {

        email.textProperty().bind(MailBoxManager.INSTANCE.mailProperty());
        MailBoxManager.INSTANCE.addDisposable(() -> email.textProperty().unbind());

        OnToReadChanged(MailBoxManager.INSTANCE.toReadProperty().getValue());

        ChangeListener<Number> readListener = (observable, oldValue, newValue) -> {
            OnToReadChanged(newValue);
        };
        MailBoxManager.INSTANCE.toReadProperty().addListener(readListener);
        MailBoxManager.INSTANCE.addDisposable(() -> MailBoxManager.INSTANCE.toReadProperty().removeListener(readListener));
    }

    private void OnToReadChanged(Number newValue) {
        Platform.runLater(() -> {
            int value = (int) newValue;
            if (value > 0) {
                defaultMenuVoice.setText(DEFAULT_TEXT + " (" + value + ")");
            } else {
                defaultMenuVoice.setText(DEFAULT_TEXT);
            }
        });
    }

    @FXML
    private void onMenuVoiceSelected(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button clickedButton) {
            selectButtonInternal(clickedButton, false);
        }
    }

    @FXML
    private void onSendClicked(ActionEvent event) {
        MailBoxManager.INSTANCE.openNewMailModal();
    }

    private void selectButtonInternal(Button clickedButton, boolean forceRefresh) {
        selectedButtonUITransition(clickedButton);
        Object targetObj = clickedButton.getUserData();
        if (targetObj instanceof String target) {
            MailBoxManager.INSTANCE.requestPage(target, forceRefresh);
        }
    }

    private void selectedButtonUITransition(Button newButton) {
        changeUIState(selectedButton, false);
        selectedButton = newButton;
        changeUIState(selectedButton, true);
    }

    private void changeUIState(Button btn, boolean isOn) {
        if (btn == null) {
            return;
        }

        if (!isOn) {
            btn.getStyleClass().remove(SELECTED_BUTTON_CLASS);
            btn.getStyleClass().add(BUTTON_CLASS);
        } else {
            btn.getStyleClass().remove(BUTTON_CLASS);
            btn.getStyleClass().add(SELECTED_BUTTON_CLASS);
        }
    }

    @FXML
    private void onLogout() {
        MailBoxManager.INSTANCE.onLogout();
    }
}
