package smalljson;

public enum JSONReadFeature {
    /**
     * // ...
     * <p>and</p>
     * /* ... &ast;/
     * <p>comments</p>
     */
    JAVA_COMMENTS,
    /**
     * If allow extra chars after the end: "[1,2,3]xxx"
     */
    EXTRA_CHARS,

    /**
     * {@code 'xyzzy' -> "xyzzy"}
     */
    SINGLE_QUOTES,
    /**
     * "\x" -> "x" (ignore unsupported characters);
     * "&bsol;u41" -> "A" (allow unicode number be less than 4 hex digits)
     */
    INVALID_ESCAPES,
    /**
     * Characters with code less than 32 can occur in string without escaping
     */
    STRING_CONTROL_CHARS,

    /**
     * Recognize {@code true/false/null} ignoring case
     */
    CASE_INSENSITIVE,

    /**
     * Recognize following numeric values (case-insensitive):
     * {@code NaN,
     * Infinity, inf,
     * +Infinity, +inf,
     * -Infinity, -inf}
     */
    NAN_INF_NUMBERS,
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
    TRAILING_DECIMAL_POINT,

    /**
     * {@code { x: "value" } -> { "x": "value" }}
     */
    UNQUOTED_FIELD_NAMES,
    /**
     * {@code { "x": 1, "x": 2 } -> { "x": 2 }}
     */
    DUPLICATE_FIELD_NAMES,
    /**
     * {@code { "x": "value", } -> { "x": "value" }}
     * {@code [1,2,] -> [1,2]}
     * <br>
     * Takes priority over {@link #ARRAY_MISSING_VALUES} if both are present.
     */
    TRAILING_COMMA,
    /**
     * {@code [1,,,2,] -> [1,null,null,2,null]}
     */
    ARRAY_MISSING_VALUES
}
