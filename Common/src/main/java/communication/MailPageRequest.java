package communication;

import java.io.Serializable;

public class MailPageRequest implements Serializable {

    private int start;
    private int quantity;
    private boolean fromReceived = true;

    public MailPageRequest() {

    }

    public MailPageRequest(int start, int quantity, boolean fromReceived) {
        this.start = start;
        this.quantity = quantity;
        this.fromReceived = fromReceived;
    }

    public int getStart() {
        return start;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setFromReceived(boolean fromReceived) {
        this.fromReceived = fromReceived;
    }

    public boolean isFromReceived() {
        return fromReceived;
    }
}
