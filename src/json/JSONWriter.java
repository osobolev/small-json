package json;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public final class JSONWriter {

    private final PrintWriter pw;
    private final String indent;
    private final String space;

    public JSONWriter(PrintWriter pw, String indent) {
        this.pw = pw;
        this.indent = indent;
        this.space = indent.isEmpty() ? "" : " ";
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

    private void writeArray(int nestingLevel, Iterable<?> collection) {
        if (!collection.iterator().hasNext()) {
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
                if (ch < ' ') {
                    unicodeEscape(buf, ch);
                } else {
                    // todo: unicode escape some extra-special characters???
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
        } else if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (map.isEmpty()) {
                print("{}");
            } else {
                println("{");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        println(",");
                    }
                    String key = String.valueOf(entry.getKey());
                    print(nestingLevel + 1, "\"" + escape(key) + "\":" + space);
                    Object value = entry.getValue();
                    write(nestingLevel + 1, value);
                }
                println("");
                print(nestingLevel, "}");
            }
        } else if (obj instanceof Iterable<?>) {
            Iterable<?> collection = (Iterable<?>) obj;
            writeArray(nestingLevel, collection);
        } else if (obj.getClass().isArray()) {
            writeArray(nestingLevel, () -> new ArrayIterator(obj));
        } else if (obj instanceof Boolean) {
            Boolean bool = (Boolean) obj;
            print(bool.toString());
        } else if (obj instanceof Number) {
            Number num = (Number) obj;
            print(num.toString());
        } else if (obj instanceof JSONRawValue) {
            JSONRawValue raw = (JSONRawValue) obj;
            print(raw.toString());
        } else {
            print("\"" + escape(obj.toString()) + "\"");
        }
    }

    public void write(Object obj) {
        write(0, obj);
        println(""); // todo: ???
        pw.flush(); // todo: ???
    }

    private static final class ArrayIterator implements Iterator<Object> {

        private final Object array;
        private final int length;
        private int i = 0;

        ArrayIterator(Object array) {
            this.array = array;
            this.length = Array.getLength(array);
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
