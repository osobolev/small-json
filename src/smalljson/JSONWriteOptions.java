package smalljson;

public final class JSONWriteOptions {

    public static final JSONWriteOptions COMPACT = new JSONWriteOptions("", ":", ",", "");
    public static final JSONWriteOptions PRETTY = pretty("    ");

    public final String indent;
    public final String colon;
    public final String comma;
    public final String eoln;

    public JSONWriteOptions(String indent, String colon, String comma, String eoln) {
        this.indent = indent;
        this.colon = colon;
        this.comma = comma;
        this.eoln = eoln;
    }

    public static JSONWriteOptions pretty(String indent) {
        return new JSONWriteOptions(indent, ": ", ",", "\n");
    }
}
