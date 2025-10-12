package communication;

public class QueryResult<T> {

    private boolean success = false;
    private String message = "";
    private T payload;

    public QueryResult() {

    }

    public QueryResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public void Success(String message) {
        success = true;
        this.message = message;
    }

    public void Error(String message) {
        success = false;
        this.message = message;
    }

    public void Success(String message, T payload) {
        Success(message);
        this.payload = payload;
    }

    public void Error(String message, T payload) {
        Error(message);
        this.payload = payload;
    }

    public void SetSuccessPayload(T payload) {
        Success("");
        this.payload = payload;
    }

    public void SetErrorPayload(T payload) {
        Error("");
        this.payload = payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isError() {
        return !success;
    }

    public String getMessage() {
        return this.message;
    }

    public T getPayload() {
        return this.payload;
    }
}
