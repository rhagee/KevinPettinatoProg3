package communication;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.UUID;

public class MailBoxMetadata implements Serializable {
    
    protected String mail;

    protected int received = 0;
    protected int toRead = 0;
    protected int sent = 0;


    public MailBoxMetadata() {
    }

    public MailBoxMetadata(String mail) {
        this.mail = mail;
    }

    public MailBoxMetadata(String mail, int received, int toRead, int sent) {
        this.mail = mail;
        this.received = received;
        this.toRead = toRead;
        this.sent = sent;
    }

    public MailBoxMetadata(MailBoxMetadata toCopy) {
        this.mail = toCopy.getMail();
        this.received = toCopy.getReceived();
        this.toRead = toCopy.getToRead();
        this.sent = toCopy.getSent();
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getReceived() {
        return received;
    }

    public void setReceived(int received) {
        this.received = received;
    }

    public int getToRead() {
        return toRead;
    }

    public void setToRead(int toRead) {
        this.toRead = toRead;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public void IncrementReceived() {
        received++;
    }

    public void DecrementReceived() {
        received--;
    }

    public void IncrementToRead() {
        toRead++;
    }

    public void DecrementToRead() {
        toRead--;
    }

    public void IncrementSent() {
        sent++;
    }

    public void DecrementSent() {
        sent--;
    }
}
