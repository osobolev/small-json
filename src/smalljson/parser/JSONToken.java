package smalljson.parser;

public final class JSONToken {

    public final JSONTokenType type;
    public final String text;
    public final Object value;
    public final long index;
    public final int line;
    public final int column;

    public JSONToken(JSONTokenType type, String text, Object value, long index, int line, int column) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.index = index;
        this.line = line;
        this.column = column;
    }

    public JSONToken(JSONTokenType type, String text, long index, int line, int column) {
        this(type, text, null, index, line, column);
    }

    @Override
    public String toString() {
        String text = this.text == null ? "" : ": \"" + this.text + "\"";
        return type + text + " at (" + line + ":" + column + ")";
    }
}
