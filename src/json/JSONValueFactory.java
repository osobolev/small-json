package json;

public class JSONValueFactory {

    public static final class RawValue implements JSONRawValue {

        public final String string;

        public RawValue(String string) {
            this.string = string;
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RawValue) {
                RawValue that = (RawValue) obj;
                return this.string.equals(that.string);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public Object rawValue(String str) {
        return new RawValue(str);
    }

    public Object stringValue(String str) {
        return str;
    }

    public Object nanValue() {
        return Double.NaN;
    }

    public Object infinityValue(int sign) {
        return sign < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

    public Object zeroValue(int sign) {
        return 0;
    }

    public Object intValue(String str) {
        return Long.valueOf(str);
    }

    public Object floatValue(String str) {
        return Double.valueOf(str);
    }

    public Object boolValue(boolean b) {
        return b;
    }

    public Object nullValue() {
        return null;
    }
}
