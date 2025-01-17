package smalljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
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
            assertEquals(1, parse(json, JSONReadFeature.JAVA_COMMENTS));
            assertThrows(JSONParseException.class, () -> parse(json));
        }
        assertThrows(JSONParseException.class, () -> parse("/* end", JSONReadFeature.JAVA_COMMENTS));
    }

    @Test
    public void testLexerError() {
        String examples = "~@#%^&()+-*/|.<>?!;\\=`";
        for (int i = 0; i < examples.length(); i++) {
            char ch = examples.charAt(i);
            assertThrows(JSONParseException.class, () -> parse(String.valueOf(ch)));
        }

        assertEquals(JSONTokenType.LCURLY, parser("{}").getCurrent().type);
        assertDoesNotThrow(() -> parser("{}").getCurrent().toString());
        assertDoesNotThrow(() -> parser("true").getCurrent().toString());

        assertThrows(
            UncheckedIOException.class,
            () -> new JSONParser(
                options(),
                new InputStream() {
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

        testLocation("[\"\uD83C\uDF55\", @", 7, 1, 7);
        testLocation("[\"\uD83C\uDF55\", \n@", 8, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \r@", 8, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \r\n@", 9, 2, 1);
        testLocation("[\"\uD83C\uDF55\", \n\r@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\", \r\r@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\", \n\n@", 9, 3, 1);
        testLocation("[\"\uD83C\uDF55\"\r,\r\n\"\uD83C\uDF55\"\n\r,\n@", 17, 6, 1);

        testLocation("[ 1, \"\uD83C\uDF55\", \"\uD83C\" ]", 12, 1, 12);
    }

    @Test
    public void testExtraChars() {
        assertEquals(1, parse("1nextra", JSONReadFeature.EXTRA_CHARS));
        assertEquals(list(1), parse("[1]extra", JSONReadFeature.EXTRA_CHARS));
        assertEquals(map("x", 1), parse("{ \"x\":1 }extra", JSONReadFeature.EXTRA_CHARS));

        assertThrows(JSONParseException.class, () -> parse("1nextra"));
        assertThrows(JSONParseException.class, () -> parse("[1]extra"));
        assertThrows(JSONParseException.class, () -> parse("{ \"x\":1 }extra"));
    }

    @Test
    public void testNestingLevels() {
        JSONParseOptions options0 = JSONParseOptions.builder().copy(options()).maxNestingLevel(0).build();
        new JSONParser(options0, "0").parse();

        assertThrows(JSONParseException.class, () -> new JSONParser(options0, "[]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options0, "{}").parse());

        JSONParseOptions options1 = JSONParseOptions.builder().copy(options()).maxNestingLevel(1).build();
        new JSONParser(options1, "0").parse();
        new JSONParser(options1, "[]").parse();
        new JSONParser(options1, "[1]").parse();
        new JSONParser(options1, "{}").parse();
        new JSONParser(options1, "{\"x\":1}").parse();

        assertThrows(JSONParseException.class, () -> new JSONParser(options1, "[[]]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options1, "[{}]").parse());

        JSONParseOptions options2 = JSONParseOptions.builder().copy(options()).maxNestingLevel(2).build();
        new JSONParser(options2, "0").parse();
        new JSONParser(options2, "[]").parse();
        new JSONParser(options2, "[]").parseArray();
        new JSONParser(options2, "[[]]").parse();
        new JSONParser(options2, "[[]]").parseArray();
        new JSONParser(options2, "[[1]]").parse();
        new JSONParser(options2, "[[1]]").parseArray();
        new JSONParser(options2, "{}").parse();
        new JSONParser(options2, "{}").parseObject();
        new JSONParser(options2, "[{}]").parse();
        new JSONParser(options2, "[{}]").parseArray();
        new JSONParser(options2, "[{\"x\":1}]").parse();
        new JSONParser(options2, "[{\"x\":1}]").parseArray();
        new JSONParser(options2, "{\"x\":[]}").parse();
        new JSONParser(options2, "{\"x\":[]}").parseObject();

        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[[[]]]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[[[]]]").parseArray());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[[{}]]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[[{}]]").parseArray());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[{\"x\":[]}]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[{\"x\":[]}]").parseArray());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[{\"x\":{}}]").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "[{\"x\":{}}]").parseArray());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "{\"x\":[{}]}").parse());
        assertThrows(JSONParseException.class, () -> new JSONParser(options2, "{\"x\":[{}]}").parseObject());
    }

    @Test
    public void testBadNesting() {
        assertThrows(JSONParseException.class, () -> parse("{[}]"));
        assertThrows(JSONParseException.class, () -> parse("[{]}"));
        assertThrows(JSONParseException.class, () -> parse("{1,2}"));
        assertThrows(JSONParseException.class, () -> parse("[\"x\":1]"));
        assertThrows(JSONParseException.class, () -> parser("{}").parseArray());
        assertThrows(JSONParseException.class, () -> parser("[]").parseObject());
    }

    @Test
    public void testValueFactory() {
        JSONValueFactory valueFactory = new JSONValueFactory() {
            @Override
            public Object intValue(String str) {
                return new BigInteger(str);
            }
        };
        JSONParseOptions options = JSONParseOptions.builder().copy(options()).valueFactory(valueFactory).build();
        assertEquals(BigInteger.ONE, new JSONParser(options, "1").parse());
    }
}
