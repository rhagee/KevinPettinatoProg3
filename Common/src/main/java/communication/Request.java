package communication;

import utils.RequestCodes;

public class Request<T> extends AbstractCommunication<T> {

    private RequestCodes code;
    protected String token;

    public Request(String id, T payload, RequestCodes code) {
        super(id, payload);
        this.code = code;
    }

    public Request(String id, T payload, RequestCodes code, String token) {
        super(id, payload);
        this.code = code;
        this.token = token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public RequestCodes getCode() {
        return this.code;
    }
}
