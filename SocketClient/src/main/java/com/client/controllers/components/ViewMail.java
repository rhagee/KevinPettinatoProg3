package com.client.controllers.components;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.EmailManagement.PageStatus;
import communication.Mail;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewMail extends Component {

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    protected String RESOURCE_NAME = "/com/prog/ui/components/view_mail.fxml";


    @FXML
    private Label sender, receivers, subject;

    @FXML
    private Button reply, replyAll, forward;

    @FXML
    private TextArea message;

    public ViewMail() {
        initializeComponent(RESOURCE_NAME);
    }

    private boolean clearOnNextOpen = true;


    @FXML
    private void initialize() {
        Hide();
        initializeBindings();
    }

    private void initializeBindings() {


        ChangeListener<Mail> selectedMailListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                Show();
                PopulateFields(newValue);
            } else {
                Hide();
            }
        };

        MailBoxManager.INSTANCE.getSelectedMailProperty().addListener(selectedMailListener);
        MailBoxManager.INSTANCE.addDisposable(() -> MailBoxManager.INSTANCE.getSelectedMailProperty().removeListener(selectedMailListener));
    }


    private void Show() {
        this.setVisible(true);
        this.setManaged(true);
    }

    private void PopulateFields(Mail newMail) {

        boolean isSent = MailBoxManager.INSTANCE.statusProperty().getValue() == PageStatus.SENT;


        reply.setVisible(!isSent);
        reply.setManaged(!isSent);
        replyAll.setVisible(!isSent);
        replyAll.setManaged(!isSent);

        sender.setText(newMail.getSender());
        subject.setText(newMail.getSubject());
        message.setText(newMail.getMessage());

        String receiversString = "";
        List<String> receiversList = newMail.getReceiverList();
        if (receiversList.size() > 1) {
            for (String receiver : receiversList) {
                receiversString += receiver + ",";
            }
        } else if (receiversList.size() == 1) {
            receiversString = receiversList.get(0);
        } else {
            receiversString = "Nessuno";
        }
        receivers.setText(receiversString);
    }

    private void Clear() {
        sender.setText("");
        receivers.setText("");
        subject.setText("");
        message.setText("");
    }


    private void Hide() {
        Clear();
        this.setVisible(false);
        this.setManaged(false);
    }

    @FXML
    private void onClose() {
        MailBoxManager.INSTANCE.closeMailDrawer();
    }

    @FXML
    private void onReply() {
        MailBoxManager.INSTANCE.onReply();
    }

    @FXML
    private void onReplyAll() {
        MailBoxManager.INSTANCE.onReplyAll();
    }

    @FXML
    private void onForward() {
        MailBoxManager.INSTANCE.onForward();
    }

    @FXML
    private void onDelete() {
        MailBoxManager.INSTANCE.onDelete();
    }
}
