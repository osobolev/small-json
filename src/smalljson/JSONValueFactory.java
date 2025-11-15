package smalljson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONValueFactory {

    public static final JSONValueFactory DEFAULT = new JSONValueFactory();

    public Object nanValue() {
        return Double.NaN;
    }

    /**
     * Value for: "inf", "+inf", "-inf"
     *
     * @param sign 0/+1/-1
     */
    public Object infinityValue(int sign) {
        return sign < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

    /**
     * Value for: "0", "+0", "-0"
     *
     * @param sign 0/+1/-1
     */
    public Object zeroValue(int sign) {
        return 0;
    }

    public static BigInteger bigIntegerValue(int sign, String digits) {
        return new BigInteger(sign < 0 ? "-" + digits : digits);
    }

    /**
     * Value for: "123"/"+123", "-123"
     *
     * @param sign +1/-1 (never 0)
     */
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
        BigInteger x = bigIntegerValue(sign, digits);
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

    public Map<String, Object> objectValue() {
        return new LinkedHashMap<>();
    }

    public List<Object> arrayValue() {
        return new ArrayList<>();
    }
}
