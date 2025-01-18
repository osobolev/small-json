package smalljson.parser;

public enum JSONTokenType {
    LCURLY("{"), RCURLY("}"), LSQUARE("["), RSQUARE("]"), COMMA(","), COLON(":"),
    TRUE("true"), FALSE("false"), NULL("null"),
    STRING, IDENT, FLOAT, INT, IDENT_FLOAT,
    EOF;

    private final String text;

    JSONTokenType(String symbol) {
        this.text = symbol == null ? name() : "'" + symbol + "'";
    }

    JSONTokenType() {
        this(null);
    }

    @Override
    public String toString() {
        return text;
    }
}
