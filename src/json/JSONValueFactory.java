package json;

public class JSONValueFactory {

    public Object nan() {
        return Double.NaN;
    }

    public Object infinity(int sign) {
        return sign < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

    public Object integer(String str) {
        return Long.valueOf(str);
    }

    public Object floating(String str) {
        return Double.valueOf(str);
    }

    public Object bool(boolean b) {
        return b;
    }

    public Object nullObject() {
        return null;
    }
}
