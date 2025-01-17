package smalljson;

public class JSONValueFactory {

    public static final JSONValueFactory DEFAULT = new JSONValueFactory();

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