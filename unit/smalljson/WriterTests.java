package smalljson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static smalljson.TestUtil.map;
import static smalljson.TestUtil.parse;

public class WriterTests {

    private static String toString(Object obj, int indent) {
        if (indent < 0) {
            return JSONWriter.toString(obj);
        } else {
            JSONWriteOptions options = indent == 0 ? JSONWriteOptions.COMPACT : JSONWriteOptions.PRETTY;
            return JSONWriter.toString(options, obj);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1})
    public void testWriter(int indent) throws IOException {
        JSONFactory options = SiteUtil.siteOptions();
        SiteUtil.scanSiteTests((name, failing, is) -> {
            if (failing)
                return;
            Object origObj = options.parse(is);
            String json1 = toString(origObj, indent);

            Object newObj = parse(json1);
            assertEquals(origObj, newObj);

            String json2 = toString(newObj, indent);
            assertEquals(json1, json2);
        });
    }

    @Test
    public void testArray() {
        int[] array = {1, 2};
        assertEquals("[1,2]", JSONWriter.toString(JSONWriteOptions.COMPACT, array));
        assertEquals("[1, 2]", JSONWriter.toString(new JSONWriteOptions("", ": ", ", ", ", ", "", 0), array));
        assertEquals("[1, 2]", JSONWriter.toString(JSONWriteOptions.pretty("  ", 80), array));

        JSONObject object = map("x", 1, "y", 2);
        assertEquals("{\"x\":1,\"y\":2}", JSONWriter.toString(JSONWriteOptions.COMPACT, object));
        assertEquals("{\"x\": 1, \"y\": 2}", JSONWriter.toString(new JSONWriteOptions("", ": ", ", ", ", ", "", 0), object));
        assertEquals("{\"x\" : 1, \"y\" : 2}", JSONWriter.toString(new JSONWriteOptions("", " : ", ", ", ", ", "", 0), object));
    }

    @Test
    public void testRaw() {
        JSONWriter.RawValue raw = () -> "xyzzy";
        assertEquals("xyzzy", JSONWriter.toString(JSONWriteOptions.COMPACT, raw));
    }
}
