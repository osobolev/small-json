package smalljson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class JSONConverter {

    public interface CastableValue {

        <T> T cast(Class<T> cls);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object convertEnum(Class<?> cls, Object value) {
        Class<Enum> enumCls = (Class<Enum>) cls;
        return Enum.valueOf(enumCls, value.toString());
    }

    @SuppressWarnings("unchecked")
    private static Object convertRaw(Class<?> cls, Object value) {
        if (value instanceof CastableValue) {
            CastableValue castable = (CastableValue) value;
            return castable.cast(cls);
        } else if (cls.isInstance(value)) {
            return value;
        } else if (cls.isEnum()) {
            return convertEnum(cls, value);
        } else if (String.class.equals(cls)) {
            return String.valueOf(value);
        } else if (Integer.class.equals(cls) || int.class.equals(cls)) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            }
        } else if (Long.class.equals(cls) || long.class.equals(cls)) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.valueOf((String) value);
            }
        } else if (Double.class.equals(cls) || double.class.equals(cls)) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.valueOf((String) value);
            }
        } else if (Boolean.class.equals(cls) || boolean.class.equals(cls)) {
            if (value instanceof Boolean) {
                return value;
            } else if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
        } else if (BigInteger.class.isAssignableFrom(cls)) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).toBigInteger();
            } else if (value instanceof Number) {
                return BigInteger.valueOf(((Number) value).longValue());
            } else if (value instanceof String) {
                return new BigInteger((String) value);
            }
        } else if (BigDecimal.class.isAssignableFrom(cls)) {
            if (value instanceof BigInteger) {
                return new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                Number number = (Number) value;
                double d = number.doubleValue();
                long l = number.longValue();
                return d == l ? BigDecimal.valueOf(l) : BigDecimal.valueOf(d);
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            }
        } else if (JSONObject.class.isAssignableFrom(cls)) {
            if (value instanceof Map) {
                return new JSONObject((Map<String, Object>) value);
            }
        } else if (JSONArray.class.isAssignableFrom(cls)) {
            if (value instanceof List) {
                return new JSONArray((List<Object>) value);
            } else if (value instanceof Object[]) {
                return new JSONArray(Arrays.asList((Object[]) value));
            }
        }
        return cls.cast(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<T> cls, Object value) {
        if (value == null)
            return null;
        return (T) convertRaw(cls, value);
    }
}
