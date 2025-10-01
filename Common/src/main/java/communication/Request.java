package communication;

import utils.RequestCodes;

public class Request<T> extends AbstractCommunication<T> {

    private RequestCodes code;

    public Request(String id, T payload, RequestCodes code) {
        super(id, payload);
        this.code = code;
    }

    public RequestCodes getCode() {
        return this.code;
    }
}
