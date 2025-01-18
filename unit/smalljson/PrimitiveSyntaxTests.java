package smalljson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static smalljson.TestUtil.map;
import static smalljson.TestUtil.parse;

public class PrimitiveSyntaxTests {

    @Test
    public void testPrimitives() {
        assertEquals(true, parse("true"));
        assertEquals(false, parse("false"));
        assertNull(parse("null"));
    }

    @Test
    public void testCaseSensitivity() {
        assertThrows(
            JSONParseException.class,
            () -> parse("TRUE")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("FALSE")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("NULL")
        );
        assertEquals(true, parse("TRUE", JSONFeature.CASE_INSENSITIVE));
        assertEquals(false, parse("FALSE", JSONFeature.CASE_INSENSITIVE));
        assertNull(parse("NULL", JSONFeature.CASE_INSENSITIVE));
    }

    @Test
    public void testKeys() {
        String[] keys = {"true", "false", "null", "TRUE", "FALSE", "NULL"};
        for (String key : keys) {
            assertEquals(
                map(key, "!@#"),
                parse("{ \"" + key + "\": \"!@#\" }")
            );
            String unquoted = "{ " + key + ": \"!@#\" }";
            assertEquals(
                map(key, "!@#"),
                parse(unquoted, JSONFeature.UNQUOTED_FIELD_NAMES)
            );
            assertThrows(
                JSONParseException.class,
                () -> parse(unquoted)
            );
        }
    }
}
