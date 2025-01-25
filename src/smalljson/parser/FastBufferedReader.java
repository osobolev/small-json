package smalljson.parser;

import java.io.IOException;
import java.io.Reader;

public final class FastBufferedReader extends FastReader {

    private final Reader input;
    private final char[] buf = new char[8192];

    private int pos = 0;
    private int len = 0;

    public FastBufferedReader(Reader input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        if (pos >= len) {
            pos = 0;
            do {
                len = input.read(buf);
            } while (len == 0);
            if (len < 0)
                return -1;
        }
        return buf[pos++];
    }
}
