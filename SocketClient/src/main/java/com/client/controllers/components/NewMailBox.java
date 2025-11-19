package com.client.controllers.components;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.EmailManagement.MailBoxManager;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewMailBox extends Component {

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    protected String RESOURCE_NAME = "/com/prog/ui/components/new_mail_box.fxml";


    @FXML
    private TextField receivers, subject;

    @FXML
    private TextArea message;

    @FXML
    private Button sendButton;


    public NewMailBox() {
        initializeComponent(RESOURCE_NAME);
    }

    private boolean clearOnNextOpen = true;


    @FXML
    private void initialize() {
        Hide();
        initializeBindings();
    }

    private void initializeBindings() {
        MailBoxManager.INSTANCE.getNewMailOpenProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Show();
            } else {
                Hide();
            }
        });

        BooleanProperty isSendingProp = MailBoxManager.INSTANCE.isSendingMailProperty();
        EnableButton(isSendingProp.getValue());
        isSendingProp.addListener((observable, oldValue, newValue) -> {
            EnableButton(newValue);
        });
    }

    private void EnableButton(boolean isSending) {
        sendButton.setDisable(isSending);
    }

    private void Show() {

        if (!clearOnNextOpen) {
            clearOnNextOpen = true;
        } else {
            Clear();
        }

        this.setVisible(true);
        this.setManaged(true);
    }

    private void Clear() {
        receivers.setText("");
        subject.setText("");
        message.setText("");
    }

    private void Hide() {
        this.setVisible(false);
        this.setManaged(false);
    }

    @FXML
    private void onSend() {
        String[] receiverList = receivers.getText().replace(" ", "").split(",");
        String subjectText = subject.getText();
        String messageText = message.getText();

        if (subjectText.isEmpty() || messageText.isEmpty() || receiverList.length == 0) {
            AlertManager.get().add("Errore", "Compilare tutti i campi per l'invio mail, inoltre la mail deve avere almeno 1 destinatario", AlertType.ERROR);
            return;
        }

        boolean allMatches = true;
        boolean itsMe = false;
        String invalidMail = "";
        HashSet<String> receiverSet = new HashSet<>();

        for (int i = 0; i < receiverList.length && allMatches && !itsMe; i++) {
            String receiver = receiverList[i];

            if (!CheckMail(receiver)) {
                allMatches = false;
                invalidMail = receiver;
                continue;
            }

            if (receiver.equals(MailBoxManager.INSTANCE.mailProperty().getValue())) {
                itsMe = true;
                continue;
            }

            receiverSet.add(receiver);
        }

        if (!allMatches) {
            AlertManager.get().add("Errore", "Mail non valida : " + invalidMail, AlertType.ERROR);
            return;
        }

        if (itsMe) {
            AlertManager.get().add("Errore", "Impossibile inviare una mail a se stessi.", AlertType.ERROR);
            return;
        }

        MailBoxManager.INSTANCE.requestMailSendInternal(new ArrayList<>(receiverSet.stream().toList()), subjectText, messageText);
    }

    private boolean CheckMail(String mail) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(mail);
        return matcher.matches();
    }

    @FXML
    private void onClose() {
        clearOnNextOpen = false;
        MailBoxManager.INSTANCE.closeNewMailModal();
    }
}
