package smalljson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
        assertEquals(true, parse("TRUE", JSONReadFeature.CASE_INSENSITIVE));
        assertEquals(false, parse("FALSE", JSONReadFeature.CASE_INSENSITIVE));
        assertNull(parse("NULL", JSONReadFeature.CASE_INSENSITIVE));
    }
}
