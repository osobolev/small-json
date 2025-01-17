package smalljson;

public enum JSONReadFeature {
    JAVA_COMMENTS,
    /**
     * "\x" -> "x" (ignore unsupported characters);
     * "&bsol;u41" -> "A" (allow unicode number be less than 4 hex digits)
     */
    INVALID_ESCAPES,
    /**
     * {@code 'xyzzy' -> "xyzzy"}
     */
    SINGLE_QUOTES,
    STRING_CONTROL_CHARS,
    /**
     * {@code { x: "value" } -> { "x": "value: }}
     */
    UNQUOTED_FIELD_NAMES,
    /**
     * {@code [1,,,2,] -> [1,null,null,2,null]}
     */
    ARRAY_MISSING_VALUES,
    /**
     * {@code { "x": "value", } -> { "x": "value" }}
     * {@code [1,2,] -> [1,2]}
     * <br>
     * Takes priority over {@link #ARRAY_MISSING_VALUES} if both are present.
     */
    TRAILING_COMMA,
    /**
     * Recognize following numeric values (case-insensitive):
     * {@code NaN,
     * Infinity, inf,
     * +Infinity, +inf,
     * -Infinity, -inf}
     */
    NAN_INF_NUMBERS,
    /**
     * Recognize {@code true/false/null} ignoring case
     */
    CASE_INSENSITIVE,
    /**
     * {@code +123 -> 123}
     */
    LEADING_PLUS_SIGN,
    /**
     * {@code 0123 -> 123}
     */
    LEADING_ZEROS,
    /**
     * {@code .123 -> 0.123}
     */
    LEADING_DECIMAL_POINT,
    /**
     * {@code 123. -> 123.0}
     */
    TRAILING_DECIMAL_POINT
}
