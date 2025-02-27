package smalljson;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestUtil {

    public static JSONArray list(Object... items) {
        return new JSONArray(Arrays.asList(items));
    }

    public static JSONObject map() {
        return new JSONObject(Collections.emptyMap());
    }

    public static JSONObject map(String key1, Object value1) {
        return new JSONObject(Collections.singletonMap(key1, value1));
    }

    public static JSONObject map(String key1, Object value1,
                                 String key2, Object value2) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return new JSONObject(map);
    }

    public static JSONParseOptions.Builder optBuilder(JSONFeature... features) {
        return JSONFactory.options().addFeatures(features);
    }

    public static JSONFactory factory(JSONFeature... features) {
        return optBuilder(features).build();
    }

    public static Object parse(String json, JSONFeature... features) {
        return factory(features).parse(json);
    }
}
