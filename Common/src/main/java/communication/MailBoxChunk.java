package communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MailBoxChunk implements Serializable {
    private UUID chunkID;
    private String mail;
    private ArrayList<Mail> mailList = new ArrayList<>();

    public MailBoxChunk() {
        this.chunkID = UUID.randomUUID();
    }

    public MailBoxChunk(String mail) {
        this.chunkID = UUID.randomUUID();
        this.mail = mail;
    }

    public MailBoxChunk(UUID chunkID, String mail, Mail first) {
        this.chunkID = UUID.randomUUID();
        this.mail = mail;
        this.mailList.add(first);
    }

    public void AddMail(Mail newMail) {
        this.mailList.addFirst(newMail);
    }

    public void RemoveMail(Mail toRemove) {
        RemoveMail(toRemove.getId());
    }

    public void RemoveMail(UUID idToRemove) {
        this.mailList.removeIf(mail -> mail.getId().equals(idToRemove));
    }

    public int size() {
        return this.mailList.size();
    }

    public int getMailFromToNumber(int start, int end) {
        return end < mailList.size() ? end - start : mailList.size() - start;
    }

    private int getClampedEnd(int end) {
        return Math.min(end, mailList.size());
    }

    public List<Mail> getMailFromTo(int start, int end) {
        return this.mailList.subList(start, getClampedEnd(end));
    }

}
