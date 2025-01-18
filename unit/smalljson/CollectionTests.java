package smalljson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionTests {

    private static JSONObject newObject(boolean useFactory) {
        return useFactory ? new JSON().newObject() : new JSONObject();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testObjectApi(boolean useFactory) {
        {
            JSONObject object = newObject(useFactory);
            assertEquals(0, object.length());
            assertTrue(object.isEmpty());
            assertFalse(object.has("field1"));
            assertNull(object.opt("field1"));
        }
        {
            JSONObject object = newObject(useFactory);
            object.put("field1", "value1");
            object.put("field2", "value2");
            assertEquals(2, object.length());
            assertFalse(object.isEmpty());
            assertTrue(object.has("field1"));
            assertFalse(object.has("field3"));
            assertEquals("value1", object.get("field1"));
            assertEquals("value2", object.get("field2"));
            assertNull(object.opt("field3"));

            Map<String, Object> map = new HashMap<>();
            map.put("field1", "value1");
            map.put("field2", "value2");
            assertEquals(map, object.toMap());

            object.remove("field1");
            assertEquals(1, object.length());
            assertFalse(object.has("field1"));
            assertNull(object.opt("field1"));
            assertEquals("value2", object.get("field2"));

            object.clear();
            assertEquals(0, object.length());
            assertTrue(object.isEmpty());
        }
        {
            JSONObject object = newObject(useFactory);
            object.putOnce("field1", "value1");
            assertEquals(1, object.length());
            assertEquals("value1", object.get("field1"));
            assertThrows(JSONRuntimeException.class, () -> object.putOnce("field1", "anotherValue"));
            assertEquals(1, object.length());
            assertEquals("value1", object.get("field1"));
        }
        {
            JSONObject object = newObject(useFactory);
            object.putOpt("field1", null);
            assertTrue(object.isEmpty());
            object.putOpt("field1", "value1");
            assertEquals(1, object.length());
            assertEquals("value1", object.get("field1"));
            object.putOpt("field1", null);
            assertEquals(1, object.length());
            assertEquals("value1", object.get("field1"));
        }
        {
            JSONObject object1 = newObject(useFactory);
            object1.put("field1", "value1");
            JSONObject object2 = newObject(useFactory);
            object2.put("field1", "value1");
            assertEquals(object1, object2);

            Set<Object> set = new HashSet<>();
            set.add(object1);
            assertTrue(set.contains(object2));
        }
    }

    private static JSONArray newArray(boolean useFactory) {
        return useFactory ? new JSON().newArray() : new JSONArray();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testArrayApi(boolean useFactory) {
        {
            JSONArray array = newArray(useFactory);
            assertEquals(0, array.length());
            assertTrue(array.isEmpty());
        }
        {
            JSONArray array = newArray(useFactory);
            array.add("value1");
            array.add("value2");
            assertEquals(2, array.length());
            assertFalse(array.isEmpty());
            assertEquals("value1", array.get(0));
            assertEquals("value2", array.get(1));

            array.remove(0);
            assertEquals(1, array.length());
            assertEquals("value2", array.get(0));

            array.clear();
            assertEquals(0, array.length());
            assertTrue(array.isEmpty());
        }
        {
            JSONArray array = newArray(useFactory);
            array.addAll("value1", "value2");
            assertEquals(2, array.length());
            assertFalse(array.isEmpty());
            assertEquals("value1", array.get(0));
            assertEquals("value2", array.get(1));

            assertEquals(Arrays.asList("value1", "value2"), array.toList());
        }
        {
            JSONArray array1 = newArray(useFactory);
            array1.add("value1");
            JSONArray array2 = newArray(useFactory);
            array2.add("value1");
            assertEquals(array1, array2);

            Set<Object> set = new HashSet<>();
            set.add(array1);
            assertTrue(set.contains(array2));
        }
    }

    @Test
    public void testRootApi() {
        JSON factory = new JSON();
        {
            String json = "{ \"x\": [1, 2, 3], \"y\": {} }";
            assertEquals(
                factory.parseObject(json),
                factory.parseObject(new StringReader(json))
            );
            assertEquals(
                factory.parseObject(json),
                factory.parseObject(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            );
            assertEquals(
                factory.parseObject(json),
                factory.parse(json)
            );
        }
        {
            String json = "[ { \"x\": [1, 2, 3] }, [4, 5, 6], \"str\" ]";
            assertEquals(
                factory.parseArray(json),
                factory.parseArray(new StringReader(json))
            );
            assertEquals(
                factory.parseArray(json),
                factory.parseArray(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            );
            assertEquals(
                factory.parseArray(json),
                factory.parse(json)
            );
        }
    }

    @Test
    public void testConversionApi() {
        JSON factory = new JSON();
        JSONObject object = factory.parseObject(
            "{" +
            "  \"int\": 1," +
            "  \"long\": 1000000000000," +
            "  \"big\": 10000000000000000000000000000000000000000," +
            "  \"float\": 1e2," +
            "  \"bool\": true," +
            "  \"enumStr\": \"RUNNABLE\"," +
            "  \"numStr\": \"123\"," +
            "  \"boolStr\": \"true\"" +
            "}"
        );
        assertEquals(1, object.get("int", int.class));
        assertEquals(1L, object.get("int", long.class));
        assertEquals(1.0, object.get("int", double.class));
        assertEquals(BigInteger.ONE, object.get("int", BigInteger.class));
        assertEquals(BigDecimal.ONE, object.get("int", BigDecimal.class));
        assertEquals("1", object.get("int", String.class));

        assertEquals(1000000000000L, object.get("long", long.class));
        assertEquals(1000000000000.0, object.get("long", double.class));
        assertEquals(BigInteger.valueOf(1000000000000L), object.get("long", BigInteger.class));
        assertEquals(BigDecimal.valueOf(1000000000000L), object.get("long", BigDecimal.class));
        assertEquals("1000000000000", object.get("long", String.class));

        assertEquals(10000000000000000000000000000000000000000.0, object.get("big", double.class));
        assertEquals(new BigInteger("10000000000000000000000000000000000000000"), object.get("big", BigInteger.class));
        assertEquals(new BigDecimal("10000000000000000000000000000000000000000"), object.get("big", BigDecimal.class));
        assertEquals("10000000000000000000000000000000000000000", object.get("big", String.class));

        assertEquals(100, object.get("float", int.class));
        assertEquals(100L, object.get("float", long.class));
        assertEquals(100.0, object.get("float", double.class));
        assertEquals(BigInteger.valueOf(100), object.get("float", BigInteger.class));
        assertEquals(BigDecimal.valueOf(100), object.get("float", BigDecimal.class));
        assertEquals("100.0", object.get("float", String.class));

        assertEquals(123, object.get("numStr", int.class));
        assertEquals(123L, object.get("numStr", long.class));
        assertEquals(123.0, object.get("numStr", double.class));
        assertEquals(BigInteger.valueOf(123), object.get("numStr", BigInteger.class));
        assertEquals(BigDecimal.valueOf(123), object.get("numStr", BigDecimal.class));
        assertEquals("123", object.get("numStr", String.class));

        assertEquals(true, object.get("bool", boolean.class));
        assertEquals("true", object.get("bool", String.class));
        assertEquals(true, object.get("boolStr", boolean.class));

        assertEquals(Thread.State.RUNNABLE, object.get("enumStr", Thread.State.class));

        assertThrows(ClassCastException.class, () -> object.get("int", Thread.class));
    }

    @Test
    public void testCustomConversionApi() {
        JSON factory = JSON
            .options()
            .valueFactory(new JSONValueFactory() {
                @Override
                public Object floatValue(String str) {
                    return new BigDecimal(str);
                }
            })
            .build();
        JSONObject object = factory.parseObject(
            "{" +
            "  \"bd\": 1e2" +
            "}"
        );
        assertEquals(100, object.get("bd", int.class));
        assertEquals(100L, object.get("bd", long.class));
        assertEquals(100.0, object.get("bd", double.class));
        assertEquals(BigInteger.valueOf(100), object.get("bd", BigInteger.class));
        assertEquals(100, object.get("bd", BigDecimal.class).intValueExact());
        assertEquals("1E+2", object.get("bd", String.class));

        JSONObject obj = factory.newObject().put("array", new Object[] {"value1", "value2"});
        assertEquals(factory.newArray().addAll("value1", "value2"), obj.get("array", JSONArray.class));
    }

    @Test
    public void testOptionsApi() {
        JSONParseOptions options1 = JSON
            .options()
            .maxNestingLevel(10)
            .feature(JSONFeature.LEADING_ZEROS)
            .buildOptions();
        JSONParseOptions options2 = options1.copy().buildOptions();
        assertEquals(options1.features, options2.features);
        assertEquals(options1.maxNestingLevel, options2.maxNestingLevel);
        assertEquals(options1.valueFactory, options2.valueFactory);

        JSONParseOptions options3 = options1
            .copy()
            .setFeatures(Collections.singletonList(JSONFeature.TRAILING_COMMA))
            .buildOptions();
        assertFalse(options3.features.contains(JSONFeature.LEADING_ZEROS));
        assertEquals(Collections.singleton(JSONFeature.TRAILING_COMMA), options3.features);
    }
}
