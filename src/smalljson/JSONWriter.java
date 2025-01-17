package smalljson;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class JSONWriter {

    public static final String DEFAULT_INDENT = "    ";

    public interface RawValue {

        String rawJsonOutput();
    }

    private final PrintWriter pw;
    private final String indent;
    private final String space;

    public JSONWriter(PrintWriter pw, String indent) {
        this.pw = pw;
        this.indent = indent;
        this.space = indent.isEmpty() ? "" : " ";
    }

    public static String toString(Object obj, String indent) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            new JSONWriter(pw, indent).write(obj);
        }
        return sw.toString();
    }

    public static String toString(Object obj) {
        return toString(obj, DEFAULT_INDENT);
    }

    private void print(int nestingLevel, String str) {
        if (!indent.isEmpty()) {
            for (int i = 0; i < nestingLevel; i++) {
                pw.print(indent);
            }
        }
        pw.print(str);
    }

    private void print(String str) {
        pw.print(str);
    }

    private void println(String str) {
        if (indent.isEmpty()) {
            pw.print(str);
        } else {
            pw.println(str);
        }
    }

    private void writeObject(int nestingLevel, boolean empty, Iterable<? extends Map.Entry<?, ?>> map) {
        if (empty) {
            print("{}");
        } else {
            println("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map) {
                if (first) {
                    first = false;
                } else {
                    println(",");
                }
                String key = JSONConverter.key(entry.getKey());
                print(nestingLevel + 1, "\"" + escape(key) + "\":" + space);
                Object value = entry.getValue();
                write(nestingLevel + 1, value);
            }
            println("");
            print(nestingLevel, "}");
        }
    }

    private void writeArray(int nestingLevel, boolean empty, Iterable<?> collection) {
        if (empty) {
            print("[]");
        } else {
            println("[");
            boolean first = true;
            for (Object item : collection) {
                if (first) {
                    first = false;
                } else {
                    println(",");
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

    private static String escape(String str) {
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

    private void write(int nestingLevel, Object obj) {
        if (obj == null) {
            print("null");
        } else if (obj instanceof JSONObject) {
            JSONObject jsobj = (JSONObject) obj;
            writeObject(nestingLevel, jsobj.isEmpty(), jsobj);
        } else if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            writeObject(nestingLevel, map.isEmpty(), map.entrySet());
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            writeArray(nestingLevel, array.isEmpty(), array);
        } else if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            writeArray(nestingLevel, collection.isEmpty(), collection);
        } else if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            writeArray(nestingLevel, length <= 0, () -> new ArrayIterator(obj, length));
        } else if (obj instanceof Boolean) {
            Boolean bool = (Boolean) obj;
            print(bool.toString());
        } else if (obj instanceof Number) {
            Number num = (Number) obj;
            print(num.toString());
        } else if (obj instanceof RawValue) {
            RawValue raw = (RawValue) obj;
            print(raw.rawJsonOutput());
        } else {
            print("\"" + escape(obj.toString()) + "\"");
        }
    }

    public void write(Object obj) {
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
