package smalljson;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static smalljson.JSONFactory.JSON;

public class DemoTests {

    /**
     * Demonstration of JSON parsing and access to JSON object fields
     */
    @Test
    public void simpleDemo() {
        // Parse JSON text (note the static import of smalljson.JSONFactory.JSON):
        JSONObject object = JSON.parseObject("{ \"id\": 123, \"name\": \"Jsonathan\" }");

        {
            // Check field values with typed get() calls:
            Integer id = object.get("id", int.class);
            String name = object.get("name", String.class);
            assertEquals(123, id);
            assertEquals("Jsonathan", name);
        }

        {
            // Check field values with untyped get() calls:
            Object id = object.get("id");
            Object name = object.get("name");
            assertEquals(123, id);
            assertEquals("Jsonathan", name);
        }

        {
            // Differences between opt() and get() methods in JSONObject

            // Method opt() returns null for missing fields:
            assertNull(object.opt("not_exists", String.class));
            // Method get() throws an exception for missing fields:
            assertThrows(JSONRuntimeException.class, () -> object.get("not_exists", String.class));
        }
    }

    /**
     * Demonstration of JSON parsing and access to JSON array items
     */
    @Test
    public void simpleArrayDemo() {
        // Parse JSON text:
        JSONArray array = JSON.parseArray("[1974, \"ABBA\"]");

        {
            // Check item values with typed get() calls:
            Integer year = array.get(0, int.class);
            String name = array.get(1, String.class);
            assertEquals(1974, year);
            assertEquals("ABBA", name);
        }

        {
            // Check item values with untyped get() calls:
            Object year = array.get(0);
            Object name = array.get(1);
            assertEquals(1974, year);
            assertEquals("ABBA", name);
        }
    }

    /**
     * Demonstration of differences in handling of 'null' and missing values in JSONObject
     */
    @Test
    public void nullMissingDemo() {
        // Parse JSON text with a field 'empty' with 'null' value:
        JSONObject object = JSON.parseObject("{ \"empty\": null }");

        // You can check if an object has a field:
        assertTrue(object.has("empty"));
        assertFalse(object.has("not_exists"));

        // Method opt() returns null for both missing fields and fields with 'null' value:
        assertNull(object.opt("empty"));
        assertNull(object.opt("not_exists"));

        // Method opt() with defaultValue parameter returns
        // defaultValue for both missing fields and fields with 'null' value:
        assertEquals("default", object.opt("empty", String.class, "default"));
        assertEquals("default", object.opt("not_exists", String.class, "default"));

        // Method get() returns null for fields with 'null' value:
        assertNull(object.get("empty", String.class));
        // Method get() throws an exception for missing fields:
        assertThrows(JSONRuntimeException.class, () -> object.get("not_exists", String.class));
    }

    /**
     * Demo of constructing a JSON object from scratch and generating JSON document from it
     */
    @Test
    public void jsonWriteDemo() {
        // Create new object:
        JSONObject object = JSON.newObject();
        // Fill it with data:
        object
            .put("id", 123)
            .put("name", "Jsonathan")
            .put("titles", JSON.newArray().addAll("King", "Emperor", "Sultan"))
            .put("address",
                 JSON.newObject()
                        .put("street", "Coronation st.")
                        .put("house", 221));

        // Convert the built object to JSON text:
        assertDoesNotThrow(() -> {
            String json = JSONWriter.toString(object);
            System.out.println(json);
        });
    }

    /**
     * Demo of conversions that can be used to retrieve data from JSONObject/JSONArray
     */
    @Test
    public void conversionDemo() {
        // Create new object:
        JSONObject object = JSON.newObject();
        // Fill it with data.
        // Note that we use simple Maps and Lists for field values, not JSONObject/JSONArray!
        Map<String, Object> address = new HashMap<>();
        address.put("street", "Coronation st.");
        address.put("house", 221);
        List<String> titles = Arrays.asList("King", "Emperor", "Sultan");
        object
            .put("id", 123)
            .put("name", "Jsonathan")
            .put("titles", titles)
            .put("address",address);

        JSONArray jsonTitles = JSON.newArray().addAll("King", "Emperor", "Sultan");
        // List that is stored in the object is automatically converted to JSONArray:
        assertEquals(jsonTitles, object.get("titles", JSONArray.class));
        JSONObject jsonAddress = JSON.newObject().put("street", "Coronation st.").put("house", 221);
        // Map that is stored in the object is automatically converted to JSONObject:
        assertEquals(jsonAddress, object.get("address", JSONObject.class));

        // You can also cast between numeric types (int, long, double, BigInteger, BigDecimal):
        Long longId = object.get("id", Long.class);
        assertEquals(123L, longId);
        BigInteger bigId = object.get("id", BigInteger.class);
        assertEquals(BigInteger.valueOf(123), bigId);

        // Any type can be cast to String:
        String stringId = object.get("id", String.class);
        assertEquals("123", stringId);

        // You can also try to cast String to numeric type (can fail):
        object.put("size", "20");
        assertEquals(20, object.get("size", Integer.class));

        // Trying to convert String to number can fail,
        // for example string "Jsonathan" cannot be converted to int:
        assertThrows(NumberFormatException.class, () -> object.get("name", int.class));

        // Convert the built object to JSON text:
        assertDoesNotThrow(() -> {
            String json = JSONWriter.toString(object);
            System.out.println(json);
        });
    }

    /**
     * Demo of parse() method which can parse arbitrary JSON
     */
    @Test
    public void parseAnyDemo() {
        {
            // Parse JSON text with object definition:
            Object value = JSON.parse("{ \"id\": 123, \"name\": \"Jsonathan\" }");
            // Object is always parsed as JSONObject:
            assertInstanceOf(JSONObject.class, value);
            // You can cast it to JSONObject and access its fields:
            assertDoesNotThrow(() -> {
                JSONObject object = (JSONObject) value;
                assertEquals(123, object.get("id", int.class));
            });
        }
        {
            // Parse JSON text with array definition:
            Object value = JSON.parse("[1974, \"ABBA\"]");
            // Array is always parsed as JSONArray:
            assertInstanceOf(JSONArray.class, value);
            // You can cast it to JSONObject and access its fields:
            assertDoesNotThrow(() -> {
                JSONArray array = (JSONArray) value;
                assertEquals(1974, array.get(0, int.class));
                assertEquals("ABBA", array.get(1, String.class));
            });
        }
        {
            // Parse JSON text with simple value:
            Object value = JSON.parse("1000");
            // Array is always parsed as JSONArray:
            assertInstanceOf(Number.class, value);
            // You can cast it to JSONObject and access its fields:
            assertDoesNotThrow(() -> {
                // By default integer numbers are put in the minimal type of (int, long, BigInteger)
                // that can contain them, but you can use Number for any of these types:
                Number number = (Number) value;
                assertEquals(1000, number.intValue());
            });
        }
    }

    /**
     * Demo of available JSON syntax extensions which can be optionally enabled
     */
    @Test
    public void parsingExtensionsDemo() {
        {
            // Enabling support for Java-style comments in JSON:
            JSONFactory factory = JSONFactory
                .options()
                .feature(JSONFeature.JAVA_COMMENTS)
                .build();
            assertEquals(
                factory.newObject(),
                factory.parse("{ /* comment */ } // another comment") // Look! It parses!
            );
        }
        {
            // Enabling support for unquoted field names and single-quoted strings:
            JSONFactory factory = JSONFactory
                .options()
                .feature(JSONFeature.SINGLE_QUOTES)
                .feature(JSONFeature.UNQUOTED_FIELD_NAMES)
                .build();
            assertEquals(
                factory.newObject().put("name", "Jsonathan"),
                factory.parse("{ name: 'Jsonathan' }") // Look! It parses!
            );
        }
        {
            // Enabling support for missing values in arrays and trailing commas in objects/arrays:
            JSONFactory factory = JSONFactory
                .options()
                .feature(JSONFeature.ARRAY_MISSING_VALUES)
                .feature(JSONFeature.TRAILING_COMMA)
                .build();
            assertEquals(
                factory.newArray().addAll(1, null, null, 2), // Trailing comma has priority over missing values
                factory.parse("[1, , , 2, ]") // Look! It parses!
            );
            assertEquals(
                factory.newObject().put("id", 123),
                factory.parse("{ \"id\": 123, }") // Look! It parses!
            );
        }
        {
            // Enabling support for non-standard numbers:
            JSONFactory factory = JSONFactory
                .options()
                .addFeatures(
                    JSONFeature.LEADING_PLUS_SIGN,
                    JSONFeature.LEADING_ZEROS,
                    JSONFeature.LEADING_DECIMAL_POINT,
                    JSONFeature.TRAILING_DECIMAL_POINT,
                    JSONFeature.NAN_INF_NUMBERS
                )
                .build();
            assertEquals(123, factory.parse("+123")); // Look! It parses!
            assertEquals(123, factory.parse("0123")); // Look! It parses!
            assertEquals(.123, factory.parse(".123")); // Look! It parses!
            assertEquals(123., factory.parse("123.")); // Look! It parses!
            assertEquals(Double.NaN, factory.parse("NaN")); // Look! It parses!
            assertEquals(Double.POSITIVE_INFINITY, factory.parse("Infinity")); // Look! It parses!
            assertEquals(Double.POSITIVE_INFINITY, factory.parse("+inf")); // Look! It parses!
            assertEquals(Double.NEGATIVE_INFINITY, factory.parse("-inf")); // Look! It parses!
        }
    }
}
