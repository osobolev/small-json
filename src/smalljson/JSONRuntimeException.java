package smalljson;

public class JSONRuntimeException extends RuntimeException {

    public JSONRuntimeException(String message) {
        super(message);
    }

    public JSONRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONRuntimeException(Throwable cause) {
        super(cause);
    }
}
