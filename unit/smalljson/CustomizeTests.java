package smalljson;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static smalljson.TestUtil.*;

public class CustomizeTests {

    @Test
    public void testValueFactory() {
        JSONValueFactory valueFactory = new JSONValueFactory() {
            @Override
            public Object intValue(int sign, String digits) {
                return bigIntegerValue(sign, digits);
            }
        };
        JSON options = optBuilder().valueFactory(valueFactory).build();
        assertEquals(BigInteger.ONE, options.parse("1"));
    }

    private static final class CustomNull implements JSONWriter.RawValue, JSONConverter.CastableValue {

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CustomNull;
        }

        @Override
        public String toString() {
            return "<null>";
        }

        @Override
        public <T> T cast(Class<T> cls) {
            return null;
        }

        @Override
        public String rawJsonOutput() {
            return "null";
        }
    }

    private static final CustomNull NULL = new CustomNull();

    @Test
    public void testCustomNull() {
        JSONValueFactory valueFactory = new JSONValueFactory() {
            @Override
            public Object nullValue() {
                return NULL;
            }
        };
        JSON options = optBuilder().valueFactory(valueFactory).build();

        JSONObject object = options.parseObject("{ \"x\": null }");
        assertEquals(NULL, object.get("x"));
        assertNull(object.get("x", String.class));
        assertEquals(map("x", null), parse(JSONWriter.toString(object)));

        JSONArray array = options.parseArray("[null, null]");
        assertEquals(NULL, array.get(0));
        assertNull(array.get(0, String.class));
        assertEquals(list(null, null), parse(JSONWriter.toString(array)));
    }
}
