package smalljson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.*;

public class ArraySyntaxTests {

    @Test
    public void testArrays() {
        assertEquals(list(), parse("[]"));
        assertEquals(list(list()), parse("[[]]"));
        assertEquals(list(1, true, "abba"), parse("[1, true, \"abba\"]"));
        assertEquals(list(null, list(list(5, list(1, 7, 8), 6), 3)), parse("[null, [[5, [1, 7, 8], 6], 3]]"));
        assertEquals(list(null, list(list(5, list(1, 7, 8), 6), 3)), factory().parseArray("[null, [[5, [1, 7, 8], 6], 3]]"));

        assertThrows(
            JSONParseException.class,
            () -> parse("[1 2]")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("[1")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("[1, ")
        );
    }

    @Test
    public void testArrayExtensions() {
        String trailing = "[1, 2, ]";
        assertEquals(list(1, 2), parse(trailing, JSONFeature.TRAILING_COMMA));
        assertEquals(list(1, 2, null), parse(trailing, JSONFeature.ARRAY_MISSING_VALUES));
        assertEquals(list(1, 2), parse(trailing, JSONFeature.TRAILING_COMMA, JSONFeature.ARRAY_MISSING_VALUES));
        assertThrows(
            JSONParseException.class,
            () -> parse(trailing)
        );

        String missing = "[1, , , 2, 3]";
        assertEquals(list(1, null, null, 2, 3), parse(missing, JSONFeature.ARRAY_MISSING_VALUES));
        assertEquals(list(1, null, null, 2, 3), parse(missing, JSONFeature.TRAILING_COMMA, JSONFeature.ARRAY_MISSING_VALUES));
        assertThrows(
            JSONParseException.class,
            () -> parse(missing)
        );
        assertThrows(
            JSONParseException.class,
            () -> parse(missing, JSONFeature.TRAILING_COMMA)
        );

        String missTrailing = "[1, , , 2, 3, ]";
        assertEquals(list(1, null, null, 2, 3, null), parse(missTrailing, JSONFeature.ARRAY_MISSING_VALUES));
        assertEquals(list(1, null, null, 2, 3), parse(missTrailing, JSONFeature.TRAILING_COMMA, JSONFeature.ARRAY_MISSING_VALUES));
        assertThrows(
            JSONParseException.class,
            () -> parse(missTrailing)
        );
        assertThrows(
            JSONParseException.class,
            () -> parse(missTrailing, JSONFeature.TRAILING_COMMA)
        );
    }
}
