package smalljson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.*;

public class NonNumericTests {

    private static final String[] NANS = {
        "NaN", "nan",
        "Infinity", "InFiNiTy", "inf", "INF"
    };
    private static final double[] NAN_VALUES = {
        Double.NaN, Double.NaN,
        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
    };

    @Test
    public void testUnsignedNanValues() {
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"value\": YesItsANumber }", JSONFeature.NAN_INF_NUMBERS)
        );
        for (int i = 0; i < NANS.length; i++) {
            String nan = NANS[i];
            double value = NAN_VALUES[i];
            String objJson = "{ \"value\": " + nan + " }";
            assertEquals(
                map("value", value),
                parse(objJson, JSONFeature.NAN_INF_NUMBERS)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(objJson)
            );

            String arrJson = "[ " + nan + " ]";
            assertEquals(
                list(value),
                parse(arrJson, JSONFeature.NAN_INF_NUMBERS)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(arrJson)
            );
        }
    }

    @Test
    public void testMinusNanValues() {
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"value\": -YesItsANumber }", JSONFeature.NAN_INF_NUMBERS)
        );
        for (int i = 0; i < NANS.length; i++) {
            String nan = NANS[i];
            double value = NAN_VALUES[i];
            String objJson = "{ \"value\": -" + nan + " }";
            if (Double.isNaN(value)) {
                assertThrows(
                    JSONParseException.class,
                    () -> parse(objJson, JSONFeature.NAN_INF_NUMBERS)
                );
            } else {
                assertEquals(
                    map("value", -value),
                    parse(objJson, JSONFeature.NAN_INF_NUMBERS)
                );
            }
            assertThrows(
                JSONParseException.class,
                () -> parse(objJson)
            );

            String arrJson = "[ -" + nan + " ]";
            if (Double.isNaN(value)) {
                assertThrows(
                    JSONParseException.class,
                    () -> parse(arrJson, JSONFeature.NAN_INF_NUMBERS)
                );
            } else {
                assertEquals(
                    list(-value),
                    parse(arrJson, JSONFeature.NAN_INF_NUMBERS)
                );
            }
            assertThrows(
                JSONParseException.class,
                () -> parse(arrJson)
            );
        }
    }

    @Test
    public void testPlusNanValues() {
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"value\": +YesItsANumber }", JSONFeature.NAN_INF_NUMBERS, JSONFeature.LEADING_PLUS_SIGN)
        );
        for (int i = 0; i < NANS.length; i++) {
            String nan = NANS[i];
            double value = NAN_VALUES[i];
            String objJson = "{ \"value\": +" + nan + " }";
            if (Double.isNaN(value)) {
                assertThrows(
                    JSONParseException.class,
                    () -> parse(objJson, JSONFeature.NAN_INF_NUMBERS, JSONFeature.LEADING_PLUS_SIGN)
                );
            } else {
                assertEquals(
                    map("value", value),
                    parse(objJson, JSONFeature.NAN_INF_NUMBERS, JSONFeature.LEADING_PLUS_SIGN)
                );
            }
            assertThrows(
                JSONParseException.class,
                () -> parse(objJson, JSONFeature.NAN_INF_NUMBERS)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(objJson)
            );

            String arrJson = "[ +" + nan + " ]";
            if (Double.isNaN(value)) {
                assertThrows(
                    JSONParseException.class,
                    () -> parse(arrJson, JSONFeature.NAN_INF_NUMBERS, JSONFeature.LEADING_PLUS_SIGN)
                );
            } else {
                assertEquals(
                    list(value),
                    parse(arrJson, JSONFeature.NAN_INF_NUMBERS, JSONFeature.LEADING_PLUS_SIGN)
                );
            }
            assertThrows(
                JSONParseException.class,
                () -> parse(arrJson, JSONFeature.NAN_INF_NUMBERS)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(arrJson)
            );
        }
    }

    @Test
    public void testNanKeys() {
        for (String nan : NANS) {
            String noSign = "{ " + nan + ": \"!@#\" }";
            assertEquals(
                map(nan, "!@#"),
                parse(noSign, JSONFeature.NAN_INF_NUMBERS, JSONFeature.UNQUOTED_FIELD_NAMES)
            );
            assertEquals(
                map(nan, "!@#"),
                parse(noSign, JSONFeature.UNQUOTED_FIELD_NAMES)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(noSign)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(noSign, JSONFeature.NAN_INF_NUMBERS)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse("{ -" + nan + ": 1 }", JSONFeature.NAN_INF_NUMBERS, JSONFeature.UNQUOTED_FIELD_NAMES)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse("{ +" + nan + ": 1 }", JSONFeature.NAN_INF_NUMBERS, JSONFeature.UNQUOTED_FIELD_NAMES, JSONFeature.LEADING_PLUS_SIGN)
            );
        }
    }
}
