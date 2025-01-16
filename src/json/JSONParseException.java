package json;

public class JSONParseException extends RuntimeException {

    public JSONParseException(JSONToken token, String message) {
        this(token.line, token.column, message);
    }

    public JSONParseException(int line, int column, String message) {
        super(message + " at " + line + ":" + column);
    }
}
