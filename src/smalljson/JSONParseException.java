package smalljson;

import smalljson.parser.JSONToken;

public class JSONParseException extends RuntimeException {

    public final long index;
    public final int line;
    public final int column;

    public JSONParseException(String message) {
        this(-1, 0, 0, message);
    }

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
        String message = super.getMessage();
        if (line > 0 && column > 0) {
            return message + " at " + line + ":" + column;
        } else if (line > 0) {
            return message + " at line " + line;
        } else {
            return message;
        }
    }
}
