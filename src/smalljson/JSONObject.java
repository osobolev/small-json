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

    /**
     * Returns {@code defaultValue} if {@link #opt(String, Class)} returns null
     */
    public <T> T opt(String key, Class<T> cls, T defaultValue) {
        T result = opt(key, cls);
        if (result == null)
            return defaultValue;
        return result;
    }

    /**
     * Returns null if:
     * <ul>
     * <li>{@code key} is not present</li>
     * <li>the value for {@code key} is JSON {@code null} and {@link JSONValueFactory#nullValue()} returns null</li>
     * </ul>
     */
    public <T> T opt(String key, Class<T> cls) {
        Object value = map.get(key);
        return JSONConverter.convert(cls, value);
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

    public <T> T get(String key, Class<T> cls) {
        checkKeyExists(key);
        return opt(key, cls);
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

    @Override
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
