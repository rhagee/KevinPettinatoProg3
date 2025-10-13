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
    }

    public MailBoxChunk(UUID id, String mail) {
        this.chunkID = id;
        this.mail = mail;
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

    public boolean RemoveMail(Mail toRemove) {
        return RemoveMail(toRemove.getId());
    }

    public boolean RemoveMail(UUID idToRemove) {
        return this.mailList.removeIf(mail -> mail.getId().equals(idToRemove));
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

    public UUID getChunkID() {
        return chunkID;
    }

    public String getMail() {
        return mail;
    }

    public List<Mail> getMailList() {
        return mailList;
    }

    public void setChunkID(UUID id) {
        this.chunkID = id;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setMailList(ArrayList<Mail> mailList) {
        this.mailList = mailList;
    }

}
