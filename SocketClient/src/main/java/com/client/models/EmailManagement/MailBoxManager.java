package com.client.models.EmailManagement;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.BackendManagement.BackendManager;
import com.client.models.SceneManagement.SceneNames;
import com.client.models.SceneManagement.SceneTransitions;
import communication.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.RequestCodes;
import utils.ResponseCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/*
 *
 * TODO:
 *  DELETE EMAIL LOGIC (BE Request, and if that's good logic to request only 1 mail and add it to the list based on page)
 *  SEND MAIL LOGIC (BE Request, and if that's good logic to add it to the list ONLY if we are in SENT and at FIRST PAGE)
 *  REPLY ALL (Create a new mail "to be sent" with every receivers (but not me) and the sender)
 *  REPLY (Create a new mail "to be sent" with only the sender as receiver)
 *  FORWARD (Create a new mail with EMPTY receivers list)
 *
 * */

public enum MailBoxManager {
    INSTANCE;

    //#region Constants
    public static final int pageSize = 25;
    //#endregion

    //#region PropertiesDeclarations
    private StringProperty mail = new SimpleStringProperty("");
    private IntegerProperty toRead = new SimpleIntegerProperty(0);
    private IntegerProperty received = new SimpleIntegerProperty(0);
    private IntegerProperty sent = new SimpleIntegerProperty(0);
    private IntegerProperty increment = new SimpleIntegerProperty(0);

    private ListProperty<Mail> mailList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty mailListSize = new SimpleIntegerProperty(0);
    private ObjectProperty<PageStatus> status = new SimpleObjectProperty<>(PageStatus.RECEIVED);
    private IntegerProperty pageNumber = new SimpleIntegerProperty(0);

    private BooleanProperty isLoadingMetadata = new SimpleBooleanProperty(false);
    private BooleanProperty isLoadingPage = new SimpleBooleanProperty(false);
    private BooleanProperty isSendingMail = new SimpleBooleanProperty(false);

    private ObjectProperty<Mail> selectedMail = new SimpleObjectProperty<>(null);
    private BooleanProperty newMailOpen = new SimpleBooleanProperty(false);
    //#endregion

    //#region Local
    private boolean isInitialized = false;
    //#endregion

    //#region PropertiesGetters
    public StringProperty mailProperty() {
        return mail;
    }

    public ObservableList<Mail> getMailList() {
        return mailList.get();
    }

    public ObjectProperty<PageStatus> statusProperty() {
        return status;
    }

    public IntegerProperty pageNumberProperty() {
        return pageNumber;
    }

    public IntegerProperty receivedProperty() {
        return received;
    }

    public IntegerProperty sentProperty() {
        return sent;
    }

    public IntegerProperty toReadProperty() {
        return toRead;
    }

    public IntegerProperty incrementProperty() {
        return increment;
    }

    public IntegerProperty mailListSizeProperty() {
        return mailListSize;
    }

    public BooleanProperty isLoadingMetadataProperty() {
        return isLoadingMetadata;
    }

    public BooleanProperty isLoadingPageProperty() {
        return isLoadingPage;
    }

    public BooleanProperty isSendingMailProperty() {
        return isSendingMail;
    }

    public ObjectProperty<Mail> getSelectedMailProperty() {
        return selectedMail;
    }

    public BooleanProperty getNewMailOpenProperty() {
        return newMailOpen;
    }
    //#endregion

    //#region PublicMethods
    public void onLogin(String inputMail) {
        //TODO: ADD REGEX to check mail
        RequestAuthInternal(inputMail);
    }

    public void refresh() {
        if (!isInitialized) {
            return;
        }

        RequestMailboxMetadataInternal();
        RequestMailPageInternal();
    }

    public void onLogout() {
        Platform.runLater(() -> {
            BackendManager.INSTANCE.clearToken();
            SceneTransitions.SlideRight(SceneNames.LOGIN);
            Clear();
        });
    }

    public void requestPage(String requestString, boolean forceRefresh) {
        try {
            PageStatus newStatus = PageStatus.valueOf(requestString);

            if (!forceRefresh && newStatus == status.getValue()) {
                return;
            }

            this.status.setValue(newStatus);
            this.pageNumber.setValue(0);
            RequestMailPageInternal();
        } catch (IllegalArgumentException ex) {
            System.err.println("Request string " + requestString + " is not a supported page!");
        }

    }


    public void requestMailSendInternal(ArrayList<String> receivers, String subject, String message) {
        SmallMail mail = new SmallMail(this.mail.getValue(), receivers, subject, message);
        RequestSendMailInternal(mail);
    }

    public void showSent() {
        this.status.setValue(PageStatus.SENT);
        this.pageNumber.setValue(0);
        RequestMailPageInternal();
    }

    public void showReceived() {
        this.status.setValue(PageStatus.RECEIVED);
        this.pageNumber.setValue(0);
        RequestMailPageInternal();
    }

    public void nextPage() {
        if (getTotalPages() <= pageNumber.getValue() + 1) {
            return;
        }

        this.pageNumber.setValue(this.pageNumber.getValue() + 1);
        RequestMailPageInternal();
    }

    public void previousPage() {
        if (pageNumber.getValue() == 0) {
            return;
        }

        this.pageNumber.setValue(this.pageNumber.getValue() - 1);
        RequestMailPageInternal();
    }

    public int getTotalPages() {
        int totalMessages = status.getValue() == PageStatus.RECEIVED ? this.received.getValue() : this.sent.getValue();
        return (int) Math.ceil(totalMessages / (pageSize * 1d));
    }

    public int getTotalMails() {
        return status.getValue() == PageStatus.RECEIVED ? this.received.getValue() : this.sent.getValue();
    }

    public boolean hasNextPage() {
        return getTotalPages() > pageNumber.getValue() + 1;
    }

    public boolean hasPreviousPage() {
        return pageNumber.getValue() > 0;
    }

    public void openNewMailModal() {
        newMailOpen.setValue(true);
    }

    public void closeNewMailModal() {
        newMailOpen.setValue(false);
    }

    public void openMailDrawer(Mail toOpen) {
        //If we open a mail that is still unreaded, we proceed notify backend and update local data
        if (toOpen.getRead()) {
            RequestReadMailInternal(toOpen);
            toOpen.setRead(true);
            this.toRead.setValue(this.toRead.getValue() - 1);
        }

        selectedMail.set(toOpen);
    }

    public void unreadMail(Mail toUnread) {
        if (!toUnread.getRead()) {
            return;
        }

        RequestUnreadMailInternal(toUnread);
        toUnread.setRead(false);
        this.toRead.setValue(toRead.getValue() + 1);
    }

    public void closeMailDrawer() {
        this.selectedMail.setValue(null);
    }

    public void mailReceived(Response<?> response) {
        if (!isInitialized) {
            return;
        }

        try {
            Mail receivedMail = (Mail) response.getPayload();

            //We have 1 more mail toRead
            this.toRead.setValue(toRead.getValue() + 1);
            this.received.setValue(received.getValue() + 1);

            //If we are in the Received view AND we are on the first page we UPDATE the current viewed lsit
            if (status.getValue() == PageStatus.RECEIVED && pageNumber.getValue() == 0) {
                this.mailList.addFirst(receivedMail);
                FixListSize();
                mailListSize.setValue(mailList.size());
            } else {
                increment.setValue(increment.getValue() + 1);
            }
        } catch (Exception e) {
            //This should catch also the bad cast exception
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Mail ricevuta ma impossibile visualizzarla, perfavore ricarica la pagina.", AlertType.ERROR);
            });
        }
    }
    //#endregion

    //#region PrivateMethods
    private void InitializeMailbox(String mail) {
        if (isInitialized) {
            return;
        }

        this.mail = new SimpleStringProperty(mail);
        status.setValue(PageStatus.RECEIVED);
        pageNumber.setValue(0);
        isInitialized = true;
        refresh();
    }

    private void Clear() {
        mail.setValue(null);
        status.setValue(PageStatus.RECEIVED);
        toRead.setValue(0);
        sent.setValue(0);
        received.setValue(0);
        mailList.setAll(new ArrayList<>());
        pageNumber.setValue(0);
        isLoadingMetadata.setValue(false);
        isLoadingPage.setValue(false);
        this.isInitialized = false;
    }

    private void FixListSize() {
        while (this.mailList.size() > pageSize) {
            this.mailList.removeLast();
        }
    }
    //#endregion

    //#region InternalRequests

    private void RequestAuthInternal(String authMail) {
        this.mail.setValue(authMail);
        CompletableFuture<Response<?>> onCompleteFuture = new CompletableFuture<>();
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.AUTH, authMail, onCompleteFuture);
        onCompleteFuture.thenAccept(onAuthCompleteHandler);
    }

    private void RequestMailboxMetadataInternal() {
        isLoadingMetadata.setValue(true);
        CompletableFuture<Response<?>> onCompleteFuture = new CompletableFuture<>();
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.MAILBOX, null, onCompleteFuture);
        onCompleteFuture.thenAccept(onMailboxMetadataReceived);
    }

    private void RequestMailPageInternal() {
        increment.setValue(0);
        isLoadingPage.setValue(true);
        MailPageRequest request = new MailPageRequest();

        //Request setup
        request.setQuantity(pageSize);
        request.setStart(pageNumber.getValue() * pageSize);
        request.setFromReceived(status.getValue() == PageStatus.RECEIVED);

        CompletableFuture<Response<?>> onCompleteFuture = new CompletableFuture<>();
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.RECEIVE, request, onCompleteFuture);
        onCompleteFuture.thenAccept(onMailPageReceived);
    }

    private void RequestReadMailInternal(Mail mail) {
        //Just fire the event so Backend gets notified
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.READ, mail, null);
    }

    private void RequestUnreadMailInternal(Mail mail) {
        //Just fire the event so Backend gets notified
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.UNREAD, mail, null);
    }

    private void RequestSendMailInternal(SmallMail mail) {
        isSendingMail.setValue(true);
        CompletableFuture<Response<?>> onCompleteFuture = new CompletableFuture<>();
        BackendManager.INSTANCE.trySubmitRequest(RequestCodes.SEND, mail, onCompleteFuture);
        onCompleteFuture.thenAccept(onMailSent);
    }
    //#endregion

    //#region RequestsCallback
    private final Consumer<Response<?>> onAuthCompleteHandler = response -> {

        if (response.getCode() == ResponseCodes.DISCONNECTED) {
            this.mail.setValue(null);
            return;
        }

        if (response.getCode() != ResponseCodes.OK) {

            this.mail.setValue(null);
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Autenticazione fallita.", AlertType.ERROR);
            });
            return;
        }

        if (!(response.getPayload() instanceof String token)) {
            this.mail.setValue(null);
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Autenticazione fallita.", AlertType.ERROR);
            });
            return;
        }

        BackendManager.INSTANCE.setToken(token);
        InitializeMailbox(this.mail.getValue());
        Platform.runLater(() -> {
            SceneTransitions.SlideLeft(SceneNames.HOME);
        });
    };

    private final Consumer<Response<?>> onMailboxMetadataReceived = response -> {
        if (response.getCode() == ResponseCodes.DISCONNECTED) {
            isLoadingMetadata.setValue(false);
            return;
        }

        if (response.getCode() != ResponseCodes.OK) {
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", response.getErrorMessage(), AlertType.ERROR);
            });
            isLoadingMetadata.setValue(false);
            return;
        }

        try {
            MailBoxMetadata metadata = (MailBoxMetadata) response.getPayload();

            this.mail.setValue(metadata.getMail());
            this.received.setValue(metadata.getReceived());
            this.sent.setValue(metadata.getSent());
            this.toRead.setValue(metadata.getToRead());
        } catch (Exception e) {
            //This should catch also the bad cast exception
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Impossibile aggiornare i metadata della mailbox", AlertType.ERROR);
            });
        } finally {
            isLoadingMetadata.setValue(false);
        }
    };

    private final Consumer<Response<?>> onMailPageReceived = response -> {

        if (response.getCode() == ResponseCodes.DISCONNECTED) {
            isLoadingPage.setValue(false);
            return;
        }

        if (response.getCode() != ResponseCodes.OK) {
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", response.getErrorMessage(), AlertType.ERROR);
            });
            isLoadingPage.setValue(false);
            return;
        }

        try {
            List<Mail> responseList = (List<Mail>) response.getPayload();
            mailListSize.setValue(responseList.size());
            mailList.setAll(responseList);
        } catch (Exception e) {
            //This should catch also the bad cast exception
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Impossibile aggiornare la lista delle e-mail", AlertType.ERROR);
            });
        } finally {
            isLoadingPage.setValue(false);
        }
    };

    private final Consumer<Response<?>> onMailSent = response -> {

        if (response.getCode() == ResponseCodes.DISCONNECTED) {
            isSendingMail.setValue(false);
            return;
        }

        if (response.getCode() != ResponseCodes.OK) {
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", response.getErrorMessage(), AlertType.ERROR);
            });
            isSendingMail.setValue(false);
            return;
        }

        try {
            Mail completeMail = (Mail) response.getPayload();

            sent.setValue(sent.getValue() + 1);
            //Add first if we are in SENT and we are at page 0
            if (status.getValue() == PageStatus.SENT && pageNumber.getValue() == 0) {
                mailList.addFirst(completeMail);
                FixListSize();
                mailListSize.setValue(mailList.size());
            } else {
                increment.setValue(increment.getValue() + 1);
            }

            AlertManager.get().add("Invio riuscito", "La mail è stata inviata con successo!", AlertType.SUCCESS);
            newMailOpen.setValue(false);
        } catch (Exception e) {
            //This should catch also the bad cast exception
            Platform.runLater(() -> {
                AlertManager.get().add("Errore", "Impossibile aggiornare la lista delle e-mail", AlertType.ERROR);
            });
        } finally {
            isSendingMail.setValue(false);
        }
    };
    //#endregion

}
