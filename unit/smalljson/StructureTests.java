package smalljson;

import org.junit.jupiter.api.Test;
import smalljson.parser.JSONParser;
import smalljson.parser.JSONTokenType;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static smalljson.TestUtil.*;

public class StructureTests {

    @Test
    public void testComments() {
        String[] examples = {
            "/* 2 */1",
            "1// 2",
            "// 2\n1",
            "// 2\r1",
            "// 2\r\n1",
            "// 2\n\r1",
            "1/* 2 */",
            "/* 2 */1// 3"
        };
        for (String json : examples) {
            assertEquals(1, parse(json, JSONFeature.JAVA_COMMENTS));
            assertThrows(JSONParseException.class, () -> parse(json));
        }
        assertThrows(JSONParseException.class, () -> parse("/* end", JSONFeature.JAVA_COMMENTS));
    }

    private static JSONParser rawParser(String json) {
        return factory().newParser(new StringReader(json));
    }

    @Test
    public void testLexerError() {
        String examples = "~@#%^&()+-*/|.<>?!;\\=`";
        for (int i = 0; i < examples.length(); i++) {
            char ch = examples.charAt(i);
            assertThrows(JSONParseException.class, () -> parse(String.valueOf(ch)));
        }

        assertEquals(JSONTokenType.LCURLY, rawParser("{}").getCurrent().type);
        assertDoesNotThrow(() -> rawParser("{}").getCurrent().toString());
        assertDoesNotThrow(() -> rawParser("true").getCurrent().toString());

        assertThrows(
            UncheckedIOException.class,
            () -> factory().parse(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("Fail");
                    }
                }
            )
        );
    }

    private static void testLocation(String json, long index, int line, int column) {
        try {
            parse(json);
            fail("Must throw JSONParseException");
        } catch (JSONParseException ex) {
            assertEquals(index, ex.index);
            assertEquals(line, ex.line);
            assertEquals(column, ex.column);
        }
    }

    @Test
    public void testLocations() {
        testLocation("@", 0, 1, 1);
        testLocation(" @", 1, 1, 2);
        testLocation("\n@", 1, 2, 1);
        testLocation("\r@", 1, 2, 1);
        testLocation("\r\n@", 2, 2, 1);
        testLocation("\n\r@", 2, 3, 1);
        testLocation("\r\r@", 2, 3, 1);
        testLocation("\n\n@", 2, 3, 1);

        testLocation("[\"\uD83C\uDF55\", @", 7, 1, 8);
        testLocation("[\"\uD83C\uDF55\", \n@", 8, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \r@", 8, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \r\n@", 9, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \n\r@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\", \r\r@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\", \n\n@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\"\r,\r\n\"\uD83C\uDF55\"\n\r,\n@", 17, 6, 1);

        testLocation("[ 1, \"\uD83C\uDF55\", \"\uD83C\"@", 14, 1, 15);
    }

    @Test
    public void testExtraChars() {
        assertEquals(1, parse("1nextra", JSONFeature.EXTRA_CHARS));
        assertEquals(list(1), parse("[1]extra", JSONFeature.EXTRA_CHARS));
        assertEquals(map("x", 1), parse("{ \"x\":1 }extra", JSONFeature.EXTRA_CHARS));

        assertThrows(JSONParseException.class, () -> parse("1nextra"));
        assertThrows(JSONParseException.class, () -> parse("[1]extra"));
        assertThrows(JSONParseException.class, () -> parse("{ \"x\":1 }extra"));
    }

    @Test
    public void testNestingLevels() {
        JSONFactory options0 = optBuilder().maxNestingLevel(0).build();
        options0.parse("0");

        assertThrows(JSONParseException.class, () -> options0.parse("[]"));
        assertThrows(JSONParseException.class, () -> options0.parse("{}"));

        JSONFactory options1 = optBuilder().maxNestingLevel(1).build();
        options1.parse("0");
        options1.parse("[]");
        options1.parse("[1]");
        options1.parse("{}");
        options1.parse("{\"x\":1}");

        assertThrows(JSONParseException.class, () -> options1.parse("[[]]"));
        assertThrows(JSONParseException.class, () -> options1.parse("[{}]"));

        JSONFactory options2 = optBuilder().maxNestingLevel(2).build();
        options2.parse("0");
        options2.parse("[]");
        options2.parseArray("[]");
        options2.parse("[[]]");
        options2.parseArray("[[]]");
        options2.parse("[[1]]");
        options2.parseArray("[[1]]");
        options2.parse("{}");
        options2.parseObject("{}");
        options2.parse("[{}]");
        options2.parseArray("[{}]");
        options2.parse("[{\"x\":1}]");
        options2.parseArray("[{\"x\":1}]");
        options2.parse("{\"x\":[]}");
        options2.parseObject("{\"x\":[]}");

        assertThrows(JSONParseException.class, () -> options2.parse("[[[]]]"));
        assertThrows(JSONParseException.class, () -> options2.parseArray("[[[]]]"));
        assertThrows(JSONParseException.class, () -> options2.parse("[[{}]]"));
        assertThrows(JSONParseException.class, () -> options2.parseArray("[[{}]]"));
        assertThrows(JSONParseException.class, () -> options2.parse("[{\"x\":[]}]"));
        assertThrows(JSONParseException.class, () -> options2.parseArray("[{\"x\":[]}]"));
        assertThrows(JSONParseException.class, () -> options2.parse("[{\"x\":{}}]"));
        assertThrows(JSONParseException.class, () -> options2.parseArray("[{\"x\":{}}]"));
        assertThrows(JSONParseException.class, () -> options2.parse("{\"x\":[{}]}"));
        assertThrows(JSONParseException.class, () -> options2.parseObject("{\"x\":[{}]}"));
    }

    @Test
    public void testBadNesting() {
        assertThrows(JSONParseException.class, () -> parse("{[}]"));
        assertThrows(JSONParseException.class, () -> parse("[{]}"));
        assertThrows(JSONParseException.class, () -> parse("{1,2}"));
        assertThrows(JSONParseException.class, () -> parse("[\"x\":1]"));
        assertThrows(JSONParseException.class, () -> factory().parseArray("{}"));
        assertThrows(JSONParseException.class, () -> factory().parseObject("[]"));
    }

    @Test
    public void testValueFactory() {
        JSONValueFactory fact = JSONValueFactory.DEFAULT;

        assertEquals(0, fact.intValue(1, "0"));
        // Border between int and long (on side of int):
        assertEquals(Integer.MAX_VALUE, fact.intValue(1, String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, fact.intValue(-1, String.valueOf(Integer.MIN_VALUE).substring(1)));
        // Border between int and long (on side of long):
        assertEquals(Integer.MAX_VALUE + 1L, fact.intValue(1, String.valueOf(Integer.MAX_VALUE + 1L)));
        assertEquals(Integer.MIN_VALUE - 1L, fact.intValue(-1, String.valueOf(Integer.MIN_VALUE - 1L).substring(1)));
        // Border between long and BigInteger (on side of long):
        assertEquals(Long.MAX_VALUE, fact.intValue(1, String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, fact.intValue(-1, String.valueOf(Long.MIN_VALUE).substring(1)));
        // Border between long and BigInteger (on side of BigInteger):
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), fact.intValue(1, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString()));
        assertEquals(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), fact.intValue(-1, BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE).toString().substring(1)));
    }
}
