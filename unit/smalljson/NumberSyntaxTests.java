package smalljson;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.parse;

public class NumberSyntaxTests {

    @Test
    public void testStandardNumbers() {
        assertEquals(0, parse("0"));
        assertEquals(0, parse("-0"));
        assertEquals(123, parse("123"));
        assertEquals(-123, parse("-123"));
        assertEquals(123.456, parse("123.456"));
        assertEquals(1e2, parse("1e2"));
        assertEquals(1e-2, parse("1e-2"));
        assertEquals(1.1e2, parse("1.1e2"));
        assertEquals(1.1e+2, parse("1.1e+2"));
        assertEquals(1.1e-2, parse("1.1e-2"));
        assertEquals(1.234567890E+34, parse("1.234567890E+34"));

        assertThrows(JSONParseException.class, () -> parse("."));
        assertThrows(JSONParseException.class, () -> parse("1e"));
        assertThrows(JSONParseException.class, () -> parse("1e+"));
        assertThrows(JSONParseException.class, () -> parse("1e-"));
    }

    private static JSONFeature[] except(JSONFeature feature) {
        Set<JSONFeature> set = EnumSet.of(
            JSONFeature.LEADING_PLUS_SIGN, JSONFeature.LEADING_ZEROS, JSONFeature.LEADING_DECIMAL_POINT, JSONFeature.TRAILING_DECIMAL_POINT
        );
        set.remove(feature);
        return set.toArray(new JSONFeature[0]);
    }

    @Test
    public void testNumberExtensions() {
        assertEquals(123, parse("0123", JSONFeature.LEADING_ZEROS));
        assertEquals(123, parse("+123", JSONFeature.LEADING_PLUS_SIGN));
        assertEquals(0, parse("+0", JSONFeature.LEADING_PLUS_SIGN));
        assertEquals(123.456e-2, parse("+123.456e-2", JSONFeature.LEADING_PLUS_SIGN));

        assertEquals(.123, parse(".123", JSONFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e2, parse(".123e2", JSONFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e+2, parse(".123e+2", JSONFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e-2, parse(".123e-2", JSONFeature.LEADING_DECIMAL_POINT));

        assertEquals(123., parse("123.", JSONFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e2, parse("123.e2", JSONFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e+2, parse("123.e+2", JSONFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e-2, parse("123.e-2", JSONFeature.TRAILING_DECIMAL_POINT));

        assertThrows(JSONParseException.class, () -> parse(".", JSONFeature.LEADING_DECIMAL_POINT, JSONFeature.TRAILING_DECIMAL_POINT));

        assertThrows(JSONParseException.class, () -> parse("0123"));
        assertThrows(JSONParseException.class, () -> parse("+123"));
        assertThrows(JSONParseException.class, () -> parse("+0"));
        assertThrows(JSONParseException.class, () -> parse(".123"));
        assertThrows(JSONParseException.class, () -> parse("123."));

        assertThrows(JSONParseException.class, () -> parse("0123", except(JSONFeature.LEADING_ZEROS)));
        assertThrows(JSONParseException.class, () -> parse("+123", except(JSONFeature.LEADING_PLUS_SIGN)));
        assertThrows(JSONParseException.class, () -> parse("+0", except(JSONFeature.LEADING_PLUS_SIGN)));
        assertThrows(JSONParseException.class, () -> parse(".123", except(JSONFeature.LEADING_DECIMAL_POINT)));
        assertThrows(JSONParseException.class, () -> parse("123.", except(JSONFeature.TRAILING_DECIMAL_POINT)));
    }
}
