package communication;

import java.io.Serializable;

public abstract class AbstractCommunication<T> implements Serializable {
    protected String requestID;
    protected T payload;

    public AbstractCommunication(String id, T payload) {
        this.payload = payload;
        this.requestID = id;
    }

    public T getPayload() {
        return this.payload;
    }

    public String getRequestID() {
        return requestID;
    }
}
