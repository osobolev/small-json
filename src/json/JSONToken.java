package json;

public final class JSONToken {

    public final JSONTokenType type;
    public final String text;
    public final Object numberValue;
    public final int line;
    public final int column;

    public JSONToken(JSONTokenType type, String text, Object numberValue, int line, int column) {
        this.type = type;
        this.text = text;
        this.numberValue = numberValue;
        this.line = line;
        this.column = column;
    }

    public JSONToken(JSONTokenType type, String text, int line, int column) {
        this(type, text, null, line, column);
    }

    @Override
    public String toString() {
        String text = this.text == null ? "" : ": \"" + this.text + "\"";
        return type + text + " at (" + line + ":" + column + ")";
    }
}
