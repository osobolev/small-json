package smalljson;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NonNumericTests {

    private static final String[] NANS = {
        "NaN", "nan",
        "Infinity", "InFiNiTy", "inf", "INF"
    };
    private static final double[] NAN_VALUES = {
        Double.NaN, Double.NaN,
        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
    };

    private static Object parse(String json, JSONReadFeature... features) {
        JSONParseOptions options = JSONParseOptions
            .builder()
            .features(features)
            .build();
        JSONParser parser = new JSONParser(options, json);
        return parser.parse();
    }

    private static void checkObject(Object value, double expected) {
        Map<?, ?> map = assertInstanceOf(Map.class, value);
        assertEquals(1, map.size());
        Object field = map.get("value");
        assertNotNull(field);
        Double v = assertInstanceOf(Double.class, field);
        assertEquals(expected, v);
    }

    private static void checkArray(Object value, double expected) {
        List<?> list = assertInstanceOf(List.class, value);
        assertEquals(1, list.size());
        Object item = list.get(0);
        assertNotNull(item);
        Double v = assertInstanceOf(Double.class, item);
        assertEquals(expected, v);
    }

    @Test
    public void testNanValues() {
        for (int i = 0; i < NANS.length; i++) {
            String nan = NANS[i];
            double expected = NAN_VALUES[i];
            {
                String noSign = "{ \"value\": " + nan + " }";
                checkObject(parse(noSign, JSONReadFeature.NAN_INF_NUMBERS), expected);
            }
            {
                String noSign = "[ " + nan + " ]";
                checkArray(parse(noSign, JSONReadFeature.NAN_INF_NUMBERS), expected);
            }
            if (Double.isNaN(expected))
                continue;
            {
                String minusSign = "{ \"value\": -" + nan + " }";
                checkObject(parse(minusSign, JSONReadFeature.NAN_INF_NUMBERS), -expected);
            }
            {
                String minusSign = "[ -" + nan + " ]";
                checkArray(parse(minusSign, JSONReadFeature.NAN_INF_NUMBERS), -expected);
            }
            {
                String plusSign = "{ \"value\": +" + nan + " }";
                checkObject(parse(plusSign, JSONReadFeature.NAN_INF_NUMBERS, JSONReadFeature.LEADING_PLUS_SIGN), expected);
            }
            {
                String plusSign = "[ +" + nan + " ]";
                checkArray(parse(plusSign, JSONReadFeature.NAN_INF_NUMBERS, JSONReadFeature.LEADING_PLUS_SIGN), expected);
            }
            assertThrows(JSONParseException.class, () -> {
                String plusSign = "{ \"value\": +" + nan + " }";
                parse(plusSign, JSONReadFeature.NAN_INF_NUMBERS);
            });
            assertThrows(JSONParseException.class, () -> {
                String plusSign = "[ +" + nan + " ]";
                parse(plusSign, JSONReadFeature.NAN_INF_NUMBERS);
            });
        }
    }

    private static void checkObjectKey(Object value, String key) {
        Map<?, ?> map = assertInstanceOf(Map.class, value);
        assertEquals(1, map.size());
        Object field = map.get(key);
        assertNotNull(field);
        Number v = assertInstanceOf(Number.class, field);
        assertEquals(1, v.intValue());
    }

    @Test
    public void testNanKeys() {
        for (String nan : NANS) {
            String noSign = "{ " + nan + ": 1 }";
            {
                checkObjectKey(parse(noSign, JSONReadFeature.NAN_INF_NUMBERS, JSONReadFeature.UNQUOTED_FIELD_NAMES), nan);
            }
            {
                checkObjectKey(parse(noSign, JSONReadFeature.UNQUOTED_FIELD_NAMES), nan);
            }
            assertThrows(JSONParseException.class, () -> {
                checkObjectKey(parse(noSign), nan);
            });
            assertThrows(JSONParseException.class, () -> {
                checkObjectKey(parse(noSign, JSONReadFeature.NAN_INF_NUMBERS), nan);
            });
            String minusSign = "{ -" + nan + ": 1 }";
            assertThrows(JSONParseException.class, () -> {
                parse(minusSign, JSONReadFeature.NAN_INF_NUMBERS, JSONReadFeature.UNQUOTED_FIELD_NAMES);
            });
            String plusSign = "{ +" + nan + ": 1 }";
            assertThrows(JSONParseException.class, () -> {
                parse(plusSign, JSONReadFeature.NAN_INF_NUMBERS, JSONReadFeature.UNQUOTED_FIELD_NAMES, JSONReadFeature.LEADING_PLUS_SIGN);
            });
        }
    }
}
