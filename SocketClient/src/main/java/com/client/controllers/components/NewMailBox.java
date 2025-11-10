package com.client.controllers.components;

import com.client.models.EmailManagement.MailBoxManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class NewMailBox extends Component
{
    protected String RESOURCE_NAME = "/com/prog/ui/components/new_mail_box.fxml";


    @FXML
    private TextField receivers,subject;

    @FXML
    private TextArea message;


    public NewMailBox() {
        initializeComponent(RESOURCE_NAME);
    }


    @FXML
    private void initialize() {
        Hide();
        initializeBindings();
    }

    private void initializeBindings(){
        System.out.println("new mail");
        MailBoxManager.INSTANCE.getNewMailOpenProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                Show();
            }
            else
            {
                Hide();
            }
        });
    }

    private void Show()
    {
        this.setVisible(true);
        this.setManaged(true);
    }

    private void Hide()
    {
        this.setVisible(false);
        this.setManaged(false);
    }
    @FXML
    private void onSend()
    {
        String[] receiverList = receivers.getText().replace(" ","").split(",");
        String subjectText = subject.getText();
        String messageText = message.getText();

        System.out.println(receiverList[1]);
    }

    @FXML
    private void onClose()
    {
        MailBoxManager.INSTANCE.closeNewMailModal();
    }
}
