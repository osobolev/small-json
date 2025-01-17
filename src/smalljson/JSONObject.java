package smalljson;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class JSONObject implements Iterable<Map.Entry<String, Object>> {

    private final Map<String, Object> map;

    public JSONObject(Map<String, Object> map) {
        this.map = map;
    }

    public JSONObject() {
        this(JSONValueFactory.DEFAULT.objectValue());
    }

    public Map<String, Object> toMap() {
        return map;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return map.entrySet().iterator();
    }

    public int length() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public Object opt(String key) {
        return map.get(key);
    }

    public <T> T opt(Class<T> cls, String key, T defaultValue) {
        Object value = map.get(key);
        T result = JSONConverter.convert(cls, value);
        if (result == null)
            return defaultValue;
        return result;
    }

    public <T> T opt(Class<T> cls, String key) {
        return opt(cls, key, null);
    }

    private void checkKeyExists(String key) {
        if (!map.containsKey(key)) {
            throw new JSONRuntimeException("Key \"" + key + "\" is not present");
        }
    }

    public Object get(String key) {
        checkKeyExists(key);
        return opt(key);
    }

    public <T> T get(Class<T> cls, String key) {
        checkKeyExists(key);
        return opt(cls, key);
    }

    public JSONObject put(String key, Object value) {
        Objects.requireNonNull(key, "Key cannot be null");
        map.put(key, value);
        return this;
    }

    public JSONObject putOnce(String key, Object value) {
        if (map.containsKey(key))
            throw new JSONRuntimeException("Duplicate key: \"" + key + "\"");
        put(key, value);
        return this;
    }

    public JSONObject putOpt(String key, Object value) {
        if (value != null) {
            put(key, value);
        }
        return this;
    }

    public void clear() {
        map.clear();
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    public String toString() {
        return map.toString();
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject that = (JSONObject) obj;
            return this.map.equals(that.map);
        } else {
            return false;
        }
    }
}
