package communication;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Accounts implements Serializable {

    private LinkedHashMap<String, UUID> mailToID = new LinkedHashMap<>();
    private LinkedHashMap<UUID, String> idToMail = new LinkedHashMap<>();

    public LinkedHashMap<String, UUID> getMailToID() {
        return mailToID;
    }

    public void setMailToID(LinkedHashMap<String, UUID> mailToID) {
        this.mailToID = mailToID;
    }

    public LinkedHashMap<UUID, String> getIdToMail() {
        return idToMail;
    }

    public void setIdToMail(LinkedHashMap<UUID, String> idToMail) {
        this.idToMail = idToMail;
    }

    public UUID getID(String mail) {
        return mailToID.get(mail);
    }

    public String getMail(UUID id) {
        return idToMail.get(id);
    }

    public boolean authMail(String mail) {
        return mailToID.containsKey(mail);
    }

    public void addMail(String mail) {
        if (mailToID.containsKey(mail)) {
            return;
        }

        UUID id = UUID.randomUUID();
        mailToID.put(mail, id);
        idToMail.put(id, mail);
    }

    public void deleteMail(String mail) {
        UUID id = mailToID.get(mail);
        if (id == null) {
            return;
        }

        idToMail.remove(id);
        mailToID.remove(mail);
    }
}
