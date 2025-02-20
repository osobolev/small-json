package smalljson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class JSONWriter {

    public static final String DEFAULT_INDENT = "    ";

    public interface RawValue {

        String rawJsonOutput();
    }

    private final Appendable out;
    private final String indent;
    private final String colon;
    private final String comma;
    private final String lineComma;
    private final String eoln;
    private final int arrayLineLimit;

    public JSONWriter(JSONWriteOptions options, Appendable out) {
        this.out = out;
        this.indent = options.indent;
        this.colon = options.colon;
        this.comma = options.comma;
        this.lineComma = options.lineComma;
        this.arrayLineLimit = options.arrayLineLimit;
        this.eoln = options.eoln;
    }

    public static void writeTo(JSONWriteOptions options, Object obj, Appendable out) throws IOException {
        new JSONWriter(options, out).write(obj);
    }

    public static void writeTo(Object obj, Appendable out) throws IOException {
        writeTo(JSONWriteOptions.COMPACT, obj, out);
    }

    public static String toString(JSONWriteOptions options, Object obj) {
        StringBuilder buf = new StringBuilder();
        try {
            writeTo(options, obj, buf);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return buf.toString();
    }

    public static String toString(Object obj) {
        return toString(JSONWriteOptions.PRETTY, obj);
    }

    private void print(int nestingLevel, String str) throws IOException {
        if (!indent.isEmpty()) {
            for (int i = 0; i < nestingLevel; i++) {
                out.append(indent);
            }
        }
        out.append(str);
    }

    private void print(String str) throws IOException {
        out.append(str);
    }

    private void println(String str) throws IOException {
        out.append(str);
        if (!indent.isEmpty()) {
            out.append(eoln);
        }
    }

    private void writeObject(int nestingLevel, boolean empty, Iterable<? extends Map.Entry<?, ?>> map) throws IOException {
        if (empty) {
            print("{}");
        } else {
            println("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map) {
                if (first) {
                    first = false;
                } else {
                    println(comma);
                }
                String key = String.valueOf(entry.getKey());
                print(nestingLevel + 1, "\"" + escape(key) + "\"");
                print(colon);
                Object value = entry.getValue();
                write(nestingLevel + 1, value);
            }
            println("");
            print(nestingLevel, "}");
        }
    }

    private static int arrayItemLen(Object value) {
        if (value == null) {
            return 4;
        } else if (value instanceof CharSequence) {
            CharSequence str = (CharSequence) value;
            return str.length() + 2;
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            return bool.booleanValue() ? 4 : 5;
        } else if (value instanceof Number) {
            Number num = (Number) value;
            return num.toString().length();
        } else {
            return -1;
        }
    }

    private boolean arrayFitsLine(int size, Iterable<?> collection) {
        if (arrayLineLimit <= 0)
            return false;
        if (size * (lineComma.length() + 1) > arrayLineLimit)
            return false;
        int sum = 0;
        for (Object item : collection) {
            int itemLen = arrayItemLen(item);
            if (itemLen < 0)
                return false;
            if (sum > 0) {
                sum += lineComma.length();
            }
            sum += itemLen;
            if (sum > arrayLineLimit)
                return false;
        }
        return true;
    }

    private void writeArray(int nestingLevel, int size, Iterable<?> collection) throws IOException {
        if (size <= 0) {
            print("[]");
        } else if (arrayFitsLine(size, collection)) {
            print("[");
            boolean first = true;
            for (Object item : collection) {
                if (first) {
                    first = false;
                } else {
                    print(lineComma);
                }
                write(nestingLevel + 1, item);
            }
            print("]");
        } else {
            println("[");
            boolean first = true;
            for (Object item : collection) {
                if (first) {
                    first = false;
                } else {
                    println(comma);
                }
                print(nestingLevel + 1, "");
                write(nestingLevel + 1, item);
            }
            println("");
            print(nestingLevel, "]");
        }
    }

    private static void unicodeEscape(StringBuilder buf, char ch) {
        String hex = Integer.toHexString(ch);
        buf.append("\\u").append("0000", 0, 4 - hex.length()).append(hex);
    }

    public static String escape(String str) {
        StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            char escape;
            if (ch == '\b') {
                escape = 'b';
            } else if (ch == '\f') {
                escape = 'f';
            } else if (ch == '\n') {
                escape = 'n';
            } else if (ch == '\r') {
                escape = 'r';
            } else if (ch == '\t') {
                escape = 't';
            } else if (ch == '"' || ch == '\\') {
                buf.append('\\').append(ch);
                continue;
            } else {
                if (ch < ' ' // control characters
                    || (ch >= 0x7F && ch < 0xC0) // part of Latin-1 Supplement
                    || ch >= 0x1C80 // most languages end here
                ) {
                    unicodeEscape(buf, ch);
                } else {
                    buf.append(ch);
                }
                continue;
            }
            buf.append('\\').append(escape);
        }
        return buf.toString();
    }

    public void write(int nestingLevel, Object value) throws IOException {
        if (value == null) {
            print("null");
        } else if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            writeObject(nestingLevel, object.isEmpty(), object);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            writeObject(nestingLevel, map.isEmpty(), map.entrySet());
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            writeArray(nestingLevel, array.length(), array);
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            writeArray(nestingLevel, collection.size(), collection);
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            writeArray(nestingLevel, length, () -> new ArrayIterator(value, length));
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            print(bool.toString());
        } else if (value instanceof Number) {
            Number num = (Number) value;
            print(num.toString());
        } else if (value instanceof RawValue) {
            RawValue raw = (RawValue) value;
            print(raw.rawJsonOutput());
        } else {
            print("\"" + escape(value.toString()) + "\"");
        }
    }

    public void write(Object obj) throws IOException {
        write(0, obj);
    }

    private static final class ArrayIterator implements Iterator<Object> {

        private final Object array;
        private final int length;
        private int i = 0;

        ArrayIterator(Object array, int length) {
            this.array = array;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            return i < length;
        }

        @Override
        public Object next() {
            return Array.get(array, i++);
        }
    }
}
