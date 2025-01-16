package json;

public enum JSONReadFeature {
    JAVA_COMMENTS,
    /**
     * "\x" -> "x" (ignore unsupported characters);
     * "&bsol;u41" -> "A" (allow unicode number be less than 4 hex digits)
     */
    INVALID_ESCAPES,
    SINGLE_QUOTES,
    STRING_CONTROL_CHARS,
    UNQUOTED_FIELD_NAMES,
    ARRAY_MISSING_VALUES,
    TRAILING_COMMA,
    /**
     * Recognize following numeric values (case-insensitive):
     * NaN,
     * Infinity, inf,
     * +Infinity, +inf,
     * -Infinity, -inf,
     */
    NAN_INF_NUMBERS,
    /**
     * Recognize true/false/null ignoring case
     */
    CASE_INSENSITIVE
    // ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS, ALLOW_LEADING_ZEROS_FOR_NUMBERS,
    // ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS
}
