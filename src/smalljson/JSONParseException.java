package smalljson;

public class JSONParseException extends RuntimeException {

    public final long index;
    public final int line;
    public final int column;

    public JSONParseException(JSONToken token, String message) {
        this(token.index, token.line, token.column, message);
    }

    public JSONParseException(long index, int line, int column, String message) {
        super(message);
        this.index = index;
        this.line = line;
        this.column = column;
    }

    public String getRawMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at " + line + ":" + column;
    }
}
