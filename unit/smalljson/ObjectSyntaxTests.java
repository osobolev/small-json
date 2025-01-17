package smalljson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.*;

public class ObjectSyntaxTests {

    @Test
    public void testObjects() {
        assertEquals(map(), parse("{}"));
        assertEquals(map("x", 1), parse("{ \"x\":1 }"));
        assertEquals(map("x", 1, "y", 2), parse("{ \"x\":1, \"y\":2 }"));
        assertEquals(map("x", 1, "y", map("z", 3)), parse("{ \"x\":1, \"y\":{ \"z\":3 } }"));
        assertEquals(map("x", 1, "y", map("z", 3)), parser().parseObject("{ \"x\":1, \"y\":{ \"z\":3 } }"));

        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\":1 \"y\":2 }")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\":1, , \"y\":2 }")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\":1")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\":1, ")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\" 1 }")
        );
        assertThrows(
            JSONParseException.class,
            () -> parse("{ \"x\"=1 }")
        );
    }

    @Test
    public void testObjectExtensions() {
        String trailing = "{ \"x\":1, \"y\":2, }";
        assertEquals(map("x", 1, "y", 2), parse(trailing, JSONReadFeature.TRAILING_COMMA));
        assertEquals(map("x", 1, "y", 2), parse(trailing, JSONReadFeature.TRAILING_COMMA, JSONReadFeature.ARRAY_MISSING_VALUES));
        assertThrows(
            JSONParseException.class,
            () -> parse(trailing)
        );
        assertThrows(
            JSONParseException.class,
            () -> parse(trailing, JSONReadFeature.ARRAY_MISSING_VALUES)
        );

        String unquoted = "{ x:1, y:2 }";
        assertEquals(map("x", 1, "y", 2), parse(unquoted, JSONReadFeature.UNQUOTED_FIELD_NAMES));
        assertThrows(
            JSONParseException.class,
            () -> parse(unquoted)
        );

        String dup = "{ \"x\":1, \"x\":2 }";
        assertEquals(map("x", 2), parse(dup, JSONReadFeature.DUPLICATE_FIELD_NAMES));
        assertThrows(
            JSONParseException.class,
            () -> parse(dup)
        );
    }
}
