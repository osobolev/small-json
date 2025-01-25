package smalljson.parser;

public final class FastStringReader extends FastReader {

    private final String str;
    private int pos = 0;

    public FastStringReader(String str) {
        this.str = str;
    }

    @Override
    public int read() {
        if (pos >= str.length())
            return -1;
        return str.charAt(pos++);
    }
}
