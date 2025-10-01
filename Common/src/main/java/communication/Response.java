package communication;

import utils.ResponseCodes;

public class Response<T> extends AbstractCommunication<T> {
    private ResponseCodes code;
    private String errorMessage;

    public Response(String id, T payload, ResponseCodes code) {
        super(id, payload);
        this.code = code;
    }

    public Response(String id, T payload, ResponseCodes code, String errorMessage) {
        super(id, payload);
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public ResponseCodes getCode() {
        return this.code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
