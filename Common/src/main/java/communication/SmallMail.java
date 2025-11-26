package communication;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SmallMail implements Serializable {

    private String sender;
    private ArrayList<String> receiverList = new ArrayList<>();
    private String subject;
    private String message;


    public SmallMail() {

    }

    public SmallMail(String sender, ArrayList<String> receiverList, String subject, String message) {
        this.sender = sender;
        this.receiverList = receiverList;
        this.subject = subject;
        this.message = message;
    }

    public SmallMail(String sender) {
        this.sender = sender;
    }

    public SmallMail(SmallMail toCopy) {
        this.sender = toCopy.sender;
        this.receiverList.addAll(toCopy.receiverList);
        this.subject = toCopy.subject;
        this.message = toCopy.message;
    }

    public void CreateEmpty(String sender) {
        this.sender = sender;
    }

    public void addReceiver(String receiver) {
        receiverList.add(receiver);
    }

    public void removeReceiver(String receiver) {
        receiverList.remove(receiver);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getReceiverList() {
        return receiverList;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}
