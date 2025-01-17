package smalljson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ObjUtil {

    public static List<Object> list(Object... items) {
        return Arrays.asList(items);
    }

    public static Map<String, Object> map(String key1, Object value1) {
        return Collections.singletonMap(key1, value1);
    }
}
