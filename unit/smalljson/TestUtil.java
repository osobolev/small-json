package smalljson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestUtil {

    public static List<Object> list(Object... items) {
        return Arrays.asList(items);
    }

    public static Map<String, Object> map(String key1, Object value1) {
        return Collections.singletonMap(key1, value1);
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
