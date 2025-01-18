package smalljson;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.parse;

public class StringSyntaxTests {

    @Test
    public void testStandardStrings() {
        assertEquals("", parse("\"\""));
        assertEquals("xyzzy", parse("\"xyzzy\""));
        assertEquals("xyzzy abba", parse("\"xyzzy abba\""));
        assertEquals("\b\f\n\r\t", parse("\"\\b\\f\\n\\r\\t\""));
        assertEquals("/ & /", parse("\"/ & \\/\""));
        assertEquals("\\", parse("\"\\\\\""));
        assertEquals("\"", parse("\"\\\"\""));
        assertEquals("\u0123\u4567\u89AB\uCDEF\uabcd\uef4A", parse("\"\\u0123\\u4567\\u89AB\\uCDEF\\uabcd\\uef4A\""));
        assertEquals("\uD83C\uDF55", parse("\"\uD83C\uDF55\""));
        assertEquals("\uD83C\uDF55", parse("\"\\uD83C\\uDF55\""));
        assertEquals("A0", parse("\"\\u00410\""));
        assertEquals("Ax", parse("\"\\u0041x\""));

        assertThrows(JSONParseException.class, () -> parse("\"\\"));
        assertThrows(JSONParseException.class, () -> parse("\"end"));
        assertThrows(JSONParseException.class, () -> parse("\"\\u01"));
        assertThrows(JSONParseException.class, () -> parse("\"\uD83C\""));
        assertThrows(JSONParseException.class, () -> parse("\"\uD83C"));
    }

    private static JSONFeature[] except(JSONFeature feature) {
        Set<JSONFeature> set = EnumSet.of(
            JSONFeature.INVALID_ESCAPES, JSONFeature.SINGLE_QUOTES, JSONFeature.STRING_CONTROL_CHARS
        );
        set.remove(feature);
        return set.toArray(new JSONFeature[0]);
    }

    @Test
    public void testStringExtensions() {
        assertEquals("x", parse("\"\\x\"", JSONFeature.INVALID_ESCAPES));
        assertEquals("A", parse("\"\\u41\"", JSONFeature.INVALID_ESCAPES));
        assertEquals("A?", parse("\"\\u41?\"", JSONFeature.INVALID_ESCAPES));
        assertEquals("single quotes", parse("'single quotes'", JSONFeature.SINGLE_QUOTES));
        assertEquals("\t", parse("\"\t\"", JSONFeature.STRING_CONTROL_CHARS));

        assertThrows(JSONParseException.class, () -> parse("\"\\x\"", except(JSONFeature.INVALID_ESCAPES)));
        assertThrows(JSONParseException.class, () -> parse("\"\\u41\"", except(JSONFeature.INVALID_ESCAPES)));
        assertThrows(JSONParseException.class, () -> parse("\"\\u41?\"", except(JSONFeature.INVALID_ESCAPES)));
        assertThrows(JSONParseException.class, () -> parse("'single quotes'", except(JSONFeature.SINGLE_QUOTES)));
        assertThrows(JSONParseException.class, () -> parse("\"\t\"", except(JSONFeature.STRING_CONTROL_CHARS)));
    }
}
