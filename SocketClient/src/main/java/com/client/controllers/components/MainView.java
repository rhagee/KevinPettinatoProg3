package com.client.controllers.components;

import com.client.models.EmailManagement.EmailItem;
import com.client.models.EmailManagement.EmailListItem;
import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.EmailManagement.PageStatus;
import communication.Mail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainView extends Component {
    protected String RESOURCE_NAME = "/com/prog/ui/components/main_view.fxml";

    @FXML
    private Label title;

    @FXML
    private ListView<Mail> emailList;

    @FXML
    private Label page;

    @FXML
    private Button prevPage, nextPage;

    @FXML
    private VBox emptyList, loadingList;

    @FXML
    private HBox pageNumberContainer;

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
            ChangePageSettings(MailBoxManager.INSTANCE.pageNumberProperty().getValue());
        });

        ChangePageSettings(MailBoxManager.INSTANCE.pageNumberProperty().getValue());
        MailBoxManager.INSTANCE.pageNumberProperty().addListener((observable, oldValue, newValue) -> {
            ChangePageSettings(newValue);
        });

        MailBoxManager.INSTANCE.receivedProperty().addListener((observable, oldValue, newValue) -> {
            ChangePageSettings(MailBoxManager.INSTANCE.pageNumberProperty().getValue());
        });

        MailBoxManager.INSTANCE.sentProperty().addListener((observable, oldValue, newValue) -> {
            ChangePageSettings(MailBoxManager.INSTANCE.pageNumberProperty().getValue());
        });

        MailBoxManager.INSTANCE.isLoadingPageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                OnRefreshStarted();
            } else {
                OnRefreshEnded();
            }
        });

        OnMailListUpdated(MailBoxManager.INSTANCE.mailListSizeProperty().getValue());
        MailBoxManager.INSTANCE.mailListSizeProperty().addListener((observable, oldValue, newValue) -> {
            OnMailListUpdated(newValue);
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

    private void ChangePageSettings(Number newPageNumber) {
        int pages = MailBoxManager.INSTANCE.getTotalPages();
        int currPage = (int) newPageNumber + 1;
        String finalText = currPage + "/" + pages;
        Platform.runLater(() -> {
            prevPage.setDisable(!MailBoxManager.INSTANCE.hasPreviousPage());
            nextPage.setDisable(!MailBoxManager.INSTANCE.hasNextPage());
            page.setText(finalText);
        });
    }

    private void OnMailListUpdated(Number mailListSize) {
        int size = (int) mailListSize;
        Platform.runLater(() -> {
            emptyList.setManaged(size == 0);
            emptyList.setVisible(size == 0);
            emailList.setManaged(size > 0);
            emailList.setVisible(size > 0);
            pageNumberContainer.setManaged(size > 0);
            pageNumberContainer.setVisible(size > 0);
            loadingList.setVisible(false);
            loadingList.setManaged(false);
        });
    }

    private void OnRefreshStarted() {
        Platform.runLater(() -> {
            emptyList.setManaged(false);
            emptyList.setVisible(false);
            emailList.setManaged(false);
            emailList.setVisible(false);
            pageNumberContainer.setManaged(false);
            pageNumberContainer.setVisible(false);
            loadingList.setVisible(true);
            loadingList.setManaged(true);
        });
    }


    private void OnRefreshEnded() {
        OnMailListUpdated(MailBoxManager.INSTANCE.mailListSizeProperty().getValue());
    }

    private void setText(String text) {
        Platform.runLater(() -> {
            title.setText(text);
        });
    }

    @FXML
    private void onPrevPage() {
        MailBoxManager.INSTANCE.previousPage();
    }

    @FXML
    private void onNextPage() {
        MailBoxManager.INSTANCE.nextPage();
    }

}
