package communication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Mail extends SmallMail implements Serializable {
    private UUID chunkID;
    private UUID id;
    private LocalDateTime dateTime;
    private Boolean read = false;
    @JsonIgnore
    private transient BooleanProperty readProp = new SimpleBooleanProperty(false);

    public Mail() {

    }

    public Mail(UUID id) {
        this.id = id;
    }

    public Mail(Mail toCopy) {
        super(toCopy);
        this.id = UUID.randomUUID();
        this.dateTime = toCopy.dateTime;
        this.read = toCopy.read;
        this.readProp.setValue(toCopy.read);
    }

    public Mail(UUID id, String sender, ArrayList<String> receiverList, String subject, LocalDateTime dateTime, String message, boolean read) {
        super(sender, receiverList, subject, message);
        this.id = id;
        this.dateTime = dateTime;
        this.read = read;
        this.readProp.setValue(read);
    }

    public Mail(String sender) {
        super(sender);
        id = UUID.randomUUID();
    }

    public Mail(SmallMail toCopy) {
        super(toCopy);
        this.id = UUID.randomUUID();
        this.read = false;
        this.readProp.setValue(false);
        this.dateTime = LocalDateTime.now();
    }

    @Override
    public void CreateEmpty(String sender) {
        super.CreateEmpty(sender);
        id = UUID.randomUUID();
        read = false;
        this.readProp.setValue(false);
    }

    public void setNow() {
        dateTime = LocalDateTime.now();
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void onUnread() {
        read = false;
        readProp.setValue(false);
    }

    public void onRead() {
        read = true;
        readProp.setValue(true);
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
        this.readProp.setValue(read);
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

    @JsonIgnore
    public BooleanProperty getReadProp() {
        return readProp;
    }

    public void syncReadProp() {
        readProp = new SimpleBooleanProperty();
        readProp.setValue(read);
    }

}
