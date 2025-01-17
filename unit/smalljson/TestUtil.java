package smalljson;

import java.util.*;

public class TestUtil {

    public static List<Object> list(Object... items) {
        return Arrays.asList(items);
    }

    public static Map<String, Object> map() {
        return Collections.emptyMap();
    }

    public static Map<String, Object> map(String key1, Object value1) {
        return Collections.singletonMap(key1, value1);
    }

    public static Map<String, Object> map(String key1, Object value1,
                                          String key2, Object value2) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private static final JSONValueFactory TEST_FACTORY = new JSONValueFactory() {

        @Override
        public Object intValue(String str) {
            return Integer.valueOf(str);
        }
    };

    public static Object parse(String json, JSONReadFeature... features) {
        JSONParseOptions options = JSONParseOptions
            .builder()
            .valueFactory(TEST_FACTORY)
            .features(features)
            .build();
        JSONParser parser = new JSONParser(options, json);
        return parser.parse();
    }
}
