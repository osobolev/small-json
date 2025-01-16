package json;

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
     * Recognize true/false/null ignoring case
     */
    CASE_INSENSITIVE
    // ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS, ALLOW_LEADING_ZEROS_FOR_NUMBERS,
    // ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS
}
