package communication;

import java.io.Serializable;

public class MailPageRequest implements Serializable {

    private int start;
    private int quantity;

    public MailPageRequest() {

    }

    public MailPageRequest(int start, int quantity) {
        this.start = start;
        this.quantity = quantity;
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
}
