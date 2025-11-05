package com.client.controllers.components;

import com.client.models.EmailManagement.EmailItem;
import com.client.models.EmailManagement.EmailListItem;
import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.EmailManagement.PageStatus;
import communication.Mail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class MainView extends Component {
    protected String RESOURCE_NAME = "/com/prog/ui/components/main_view.fxml";

    @FXML
    private Label title;

    @FXML
    private ListView<Mail> emailList;

    public MainView() {
        initializeComponent(RESOURCE_NAME);
    }


    @FXML
    private void initialize() {
        initializeBindings();
    }

    private void initializeBindings() {
        ChangeTitle(MailBoxManager.INSTANCE.statusProperty().getValue());
        MailBoxManager.INSTANCE.statusProperty().addListener((observable, oldValue, newValue) -> {
            ChangeTitle(newValue);
        });

        emailList.setItems(MailBoxManager.INSTANCE.getMailList());
        emailList.setCellFactory(_ -> new EmailListItem());
    }

    private void ChangeTitle(PageStatus newValue) {
        if (title == null) {
            return;
        }

        String finalText = "";
        switch (newValue) {
            case PageStatus.RECEIVED:
                finalText = "Ricevute";
                break;
            case PageStatus.SENT:
                finalText = "Inviate";
                break;
            default:
                finalText = "";
                break;
        }

        setText(finalText);
    }

    private void setText(String text) {
        Platform.runLater(() -> {
            title.setText(text);
        });
    }

}
