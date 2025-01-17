package smalljson;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.*;

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

    private static JSONReadFeature[] except(JSONReadFeature feature) {
        Set<JSONReadFeature> set = EnumSet.of(
            JSONReadFeature.LEADING_PLUS_SIGN, JSONReadFeature.LEADING_ZEROS, JSONReadFeature.LEADING_DECIMAL_POINT, JSONReadFeature.TRAILING_DECIMAL_POINT
        );
        set.remove(feature);
        return set.toArray(new JSONReadFeature[0]);
    }

    @Test
    public void testNumberExtensions() {
        assertEquals(123, parse("0123", JSONReadFeature.LEADING_ZEROS));
        assertEquals(123, parse("+123", JSONReadFeature.LEADING_PLUS_SIGN));
        assertEquals(0, parse("+0", JSONReadFeature.LEADING_PLUS_SIGN));
        assertEquals(123.456e-2, parse("+123.456e-2", JSONReadFeature.LEADING_PLUS_SIGN));

        assertEquals(.123, parse(".123", JSONReadFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e2, parse(".123e2", JSONReadFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e+2, parse(".123e+2", JSONReadFeature.LEADING_DECIMAL_POINT));
        assertEquals(.123e-2, parse(".123e-2", JSONReadFeature.LEADING_DECIMAL_POINT));

        assertEquals(123., parse("123.", JSONReadFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e2, parse("123.e2", JSONReadFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e+2, parse("123.e+2", JSONReadFeature.TRAILING_DECIMAL_POINT));
        assertEquals(123.e-2, parse("123.e-2", JSONReadFeature.TRAILING_DECIMAL_POINT));

        assertThrows(JSONParseException.class, () -> parse(".", JSONReadFeature.LEADING_DECIMAL_POINT, JSONReadFeature.TRAILING_DECIMAL_POINT));

        assertThrows(JSONParseException.class, () -> parse("0123"));
        assertThrows(JSONParseException.class, () -> parse("+123"));
        assertThrows(JSONParseException.class, () -> parse("+0"));
        assertThrows(JSONParseException.class, () -> parse(".123"));
        assertThrows(JSONParseException.class, () -> parse("123."));

        assertThrows(JSONParseException.class, () -> parse("0123", except(JSONReadFeature.LEADING_ZEROS)));
        assertThrows(JSONParseException.class, () -> parse("+123", except(JSONReadFeature.LEADING_PLUS_SIGN)));
        assertThrows(JSONParseException.class, () -> parse("+0", except(JSONReadFeature.LEADING_PLUS_SIGN)));
        assertThrows(JSONParseException.class, () -> parse(".123", except(JSONReadFeature.LEADING_DECIMAL_POINT)));
        assertThrows(JSONParseException.class, () -> parse("123.", except(JSONReadFeature.TRAILING_DECIMAL_POINT)));
    }
}
