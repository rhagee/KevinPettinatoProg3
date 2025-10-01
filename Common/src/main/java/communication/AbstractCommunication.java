package communication;

public abstract class AbstractCommunication<T> {
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
