package communication;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Mail extends SmallMail implements Serializable {
    private UUID chunkID;
    private UUID id;
    private LocalDateTime dateTime;
    private boolean read;

    public Mail() {
        
    }

    public Mail(Mail toCopy) {
        super(toCopy);
        this.id = UUID.randomUUID();
        this.dateTime = toCopy.dateTime;
        this.read = toCopy.read;
    }

    public Mail(UUID id, String sender, ArrayList<String> receiverList, String subject, LocalDateTime dateTime, String message, boolean read) {
        super(sender, receiverList, subject, message);
        this.id = id;
        this.dateTime = dateTime;
        this.read = read;
    }

    public Mail(String sender) {
        super(sender);
        id = UUID.randomUUID();
    }

    public Mail(SmallMail toCopy) {
        super(toCopy);
        this.id = UUID.randomUUID();
        this.read = false;
        this.dateTime = LocalDateTime.now();
    }

    @Override
    public void CreateEmpty(String sender) {
        super.CreateEmpty(sender);
        id = UUID.randomUUID();
        read = false;
    }

    public void SetNow() {
        dateTime = LocalDateTime.now();
    }

    public void OnUnread() {
        read = false;
    }

    public void OnRead() {
        read = true;
    }

    public UUID getId() {
        return id;
    }

    public void setChunkID(UUID chunkID) {
        this.chunkID = chunkID;
    }

    public UUID getChunkID() {
        return chunkID;
    }

}
