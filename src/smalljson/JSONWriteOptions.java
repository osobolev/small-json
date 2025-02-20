package smalljson;

public final class JSONWriteOptions {

    public static final JSONWriteOptions COMPACT = new JSONWriteOptions("", ":", ",", ",", "", 0);
    public static final JSONWriteOptions PRETTY = pretty("    ");

    public final String indent;
    public final String colon;
    public final String comma;
    public final String lineComma;
    public final String eoln;
    public final int arrayLineLimit;

    public JSONWriteOptions(String indent, String colon, String comma, String lineComma, String eoln, int arrayLineLimit) {
        this.indent = indent;
        this.colon = colon;
        this.comma = comma;
        this.lineComma = lineComma;
        this.eoln = eoln;
        this.arrayLineLimit = arrayLineLimit;
    }

    public static JSONWriteOptions pretty(String indent, int arrayLineLimit) {
        return new JSONWriteOptions(indent, ": ", ",", ", ", "\n", arrayLineLimit);
    }

    public static JSONWriteOptions pretty(String indent) {
        return pretty(indent, 80);
    }
}
