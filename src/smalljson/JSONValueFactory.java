package smalljson;

import java.math.BigInteger;

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

    public Object intValue(int sign, String digits) {
        if (digits.length() <= 9) {
            return Integer.parseInt(digits) * sign;
        } else if (digits.length() <= 18) {
            long x = Long.parseLong(digits) * sign;
            if (x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE) {
                return (int) x;
            } else {
                return x;
            }
        }
        BigInteger x = new BigInteger(sign < 0 ? "-" + digits : digits);
        if (x.bitLength() <= 63) {
            return x.longValue();
        } else {
            return x;
        }
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
