package smalljson;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class JSONArray implements Iterable<Object> {

    private final List<Object> list;

    public JSONArray(List<Object> list) {
        this.list = list;
    }

    public JSONArray() {
        this(JSONValueFactory.DEFAULT.arrayValue());
    }

    public List<Object> toList() {
        return list;
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    public int length() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Object get(int index) {
        return list.get(index);
    }

    public <T> T get(int index, Class<T> cls, T defaultValue) {
        T result = get(index, cls);
        if (result == null)
            return defaultValue;
        return result;
    }

    public <T> T get(int index, Class<T> cls) {
        Object item = list.get(index);
        return JSONConverter.convert(cls, item);
    }

    public JSONArray add(Object value) {
        list.add(value);
        return this;
    }

    public JSONArray addAll(Object... values) {
        list.addAll(Arrays.asList(values));
        return this;
    }

    public void clear() {
        list.clear();
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public String toString() {
        return list.toString();
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONArray) {
            JSONArray that = (JSONArray) obj;
            return this.list.equals(that.list);
        } else {
            return false;
        }
    }
}
