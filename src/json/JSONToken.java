package json;

public final class JSONToken {

    public final JSONTokenType type;
    public final String text;
    public final int line;
    public final int column;

    public JSONToken(JSONTokenType type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return type + ": \"" + text + "\" at (" + line + ":" + column + ")";
    }
}
