package communication;

import java.io.Serializable;

//Abstract communication Class in case will ever need it
//Most likely will never use directly, it is useful to avoid repeating code in both Request and Response
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

    public boolean CheckPayloadType(Class<?> expected) {
        return expected.isInstance(payload.getClass());
    }
}
