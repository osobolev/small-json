package json;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public final class JSONLexer {

    private final Reader input;
    private final boolean keepStrings;
    private final JSONValueFactory valueFactory;
    private final boolean comments;
    private final boolean singleQuotes;
    private final boolean invalidEscapes;
    private final boolean unescapedControls;
    private final boolean caseInsensitive;
    private final boolean specialNumbers;
    private final boolean leadingPlus;
    private final boolean leadingZeros;
    private final boolean leadingPoint;
    private final boolean trailingPoint;

    private int line = 1;
    private int column = 1;
    private int ch0;
    private int ch1;
    private int ch2;

    public JSONLexer(JSONParseOptions options, Reader input) {
        this.input = input;
        this.keepStrings = options.keepStrings;
        this.valueFactory = options.valueFactory;
        this.comments = options.features.contains(JSONReadFeature.JAVA_COMMENTS);
        this.singleQuotes = options.features.contains(JSONReadFeature.SINGLE_QUOTES);
        this.invalidEscapes = options.features.contains(JSONReadFeature.INVALID_ESCAPES);
        this.unescapedControls = options.features.contains(JSONReadFeature.STRING_CONTROL_CHARS);
        this.caseInsensitive = options.features.contains(JSONReadFeature.CASE_INSENSITIVE);
        this.specialNumbers = options.features.contains(JSONReadFeature.NAN_INF_NUMBERS);
        this.leadingPlus = options.features.contains(JSONReadFeature.LEADING_PLUS_SIGN);
        this.leadingZeros = options.features.contains(JSONReadFeature.LEADING_ZEROS);
        this.leadingPoint = options.features.contains(JSONReadFeature.LEADING_DECIMAL_POINT);
        this.trailingPoint = options.features.contains(JSONReadFeature.TRAILING_DECIMAL_POINT);

        this.ch0 = -1;
        this.ch1 = nextCodepoint(-1, -1);
        this.ch2 = nextCodepoint(-1, ch1);
    }

    private void moveLocation(int ch0, int ch1) {
        if (ch1 == '\r' || ch1 == '\n') {
            if (!(ch0 == '\r' && ch1 == '\n')) {
                line++;
                column = 1;
            }
        } else if (ch1 >= 0) {
            column++;
        }
    }

    private int nextCodepoint(int ch0, int ch1) {
        try {
            int c1 = input.read();
            if (c1 < 0)
                return -1;
            if (!Character.isHighSurrogate((char) c1))
                return c1;
            int c2 = input.read();
            String error;
            if (c2 < 0) {
                error = "Unexpected EOF after Unicode high surrogate";
            } else {
                if (Character.isLowSurrogate((char) c2)) {
                    return Character.toCodePoint((char) c1, (char) c2);
                } else {
                    error = "Missing Unicode low surrogate";
                }
            }
            moveLocation(ch0, ch1);
            throw new JSONParseException(line, column, error);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int ch() {
        return ch1;
    }

    private void next() {
        moveLocation(ch0, ch1);
        ch0 = ch1;
        ch1 = ch2;
        ch2 = nextCodepoint(ch0, ch1);
    }

    private void skipSpaces() {
        while (true) {
            int ch = ch();
            if (ch < 0)
                break;
            if (comments) {
                if (ch1 == '/' && ch2 == '/') {
                    next();
                    next();
                    while (ch1 >= 0) {
                        if (ch1 == '\n') {
                            next();
                            break;
                        }
                        next();
                    }
                    continue;
                } else if (ch1 == '/' && ch2 == '*') {
                    next();
                    next();
                    boolean finished = false;
                    while (ch1 >= 0) {
                        if (ch1 == '*' && ch2 == '/') {
                            finished = true;
                            next();
                            next();
                            break;
                        }
                        next();
                    }
                    if (!finished) {
                        throw new JSONParseException(line, column, "Comment is not closed");
                    }
                    continue;
                }
            }
            if (ch > ' ')
                break;
            next();
        }
    }

    private static final Map<Integer, JSONTokenType> SYMBOLS = new HashMap<>();

    static {
        SYMBOLS.put((int) '{', JSONTokenType.LCURLY);
        SYMBOLS.put((int) '}', JSONTokenType.RCURLY);
        SYMBOLS.put((int) '[', JSONTokenType.LSQUARE);
        SYMBOLS.put((int) ']', JSONTokenType.RSQUARE);
        SYMBOLS.put((int) ',', JSONTokenType.COMMA);
        SYMBOLS.put((int) ':', JSONTokenType.COLON);
    }

    private void parseEscape(StringBuilder buf) {
        int ch = ch();
        int escape;
        if (ch == '"' || ch == '\\' || ch == '/') {
            escape = ch;
        } else if (ch == 'b') {
            escape = '\b';
        } else if (ch == 'f') {
            escape = '\f';
        } else if (ch == 'n') {
            escape = '\n';
        } else if (ch == 'r') {
            escape = '\r';
        } else if (ch == 't') {
            escape = '\t';
        } else if (ch == 'u') {
            next();
            int line = this.line;
            int column = this.column;
            int ndigits = 0;
            int unicode = 0;
            while (ndigits < 4) {
                int uch = ch();
                int digit = Character.digit(uch, 16);
                if (digit >= 0) {
                    next();
                    ndigits++;
                    unicode = (unicode << 4) + digit;
                } else {
                    break;
                }
            }
            if (!invalidEscapes && ndigits != 4) {
                throw new JSONParseException(line, column, "Invalid unicode escape sequence");
            }
            buf.append((char) unicode);
            return;
        } else {
            if (!invalidEscapes) {
                throw new JSONParseException(line, column, "Invalid escape sequence");
            }
            escape = ch;
        }
        next();
        buf.appendCodePoint(escape);
    }

    private String parseString(int quote) {
        next();
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = ch();
            if (ch < 0) {
                throw new JSONParseException(line, column, "String is not terminated");
            }
            if (ch == quote) {
                next();
                break;
            }
            if (ch == '\\') {
                next();
                parseEscape(buf);
                continue;
            }
            if (!unescapedControls && ch < ' ') {
                throw new JSONParseException(line, column, "Non-escaped control character");
            }
            next();
            buf.appendCodePoint(ch);
        }
        return buf.toString();
    }

    private enum Digits {
        NONE, ONLY_ZERO, HAS_NON_ZERO
    }

    private Digits readDigits(StringBuilder buf) {
        boolean hasAny = false;
        boolean hasNonZero = false;
        while (true) {
            int ch = ch();
            if (ch >= '0' && ch <= '9') {
                next();
                buf.append((char) ch);
                hasAny = true;
                if (ch != '0') {
                    hasNonZero = true;
                }
            } else {
                break;
            }
        }
        if (!hasAny)
            return Digits.NONE;
        return hasNonZero ? Digits.HAS_NON_ZERO : Digits.ONLY_ZERO;
    }

    private static boolean isInfinity(String ident) {
        return "Infinity".equalsIgnoreCase(ident) || "inf".equalsIgnoreCase(ident);
    }

    private JSONToken parseNumber(int line, int column) {
        int isign = 0;
        String strSign = "";
        if (ch() == '+') {
            if (!leadingPlus) {
                throw new JSONParseException(line, column, "Plus sign is not allowed");
            }
            next();
            isign = +1;
            strSign = "+";
        } else if (ch() == '-') {
            next();
            isign = -1;
            strSign = "-";
        }
        if (specialNumbers) {
            int ch = ch();
            if (ch >= 0 && Character.isJavaIdentifierStart(ch)) {
                String ident = parseIdent();
                if (isInfinity(ident)) {
                    Object value;
                    if (keepStrings) {
                        value = strSign + ident;
                    } else {
                        value = valueFactory.infinityValue(isign);
                    }
                    return new JSONToken(JSONTokenType.FLOAT, null, value, line, column);
                } else {
                    throw new JSONParseException(line, column, "Invalid infinite number");
                }
            }
        }
        StringBuilder buf = new StringBuilder();
        Digits digits1 = readDigits(buf);
        if (!leadingPoint && digits1 == Digits.NONE) {
            throw new JSONParseException(line, column, "Leading decimal point is not allowed");
        }
        if (!leadingZeros && buf.length() > 1 && buf.charAt(0) == '0') {
            throw new JSONParseException(line, column, "Leading zeros are not allowed");
        }
        boolean hasDigits = digits1 != Digits.NONE;
        boolean floating = false;
        if (ch() == '.') {
            next();
            floating = true;
            buf.append('.');
            Digits digits2 = readDigits(buf);
            if (digits2 == Digits.NONE) {
                if (!trailingPoint) {
                    throw new JSONParseException(line, column, "Trailing decimal point is not allowed");
                }
            } else {
                hasDigits = true;
            }
        }
        if (!hasDigits) {
            throw new JSONParseException(line, column, "Number must have at least one digit");
        }
        int ech = ch();
        if (ech == 'e' || ech == 'E') {
            next();
            floating = true;
            buf.append((char) ech);
            if (ch() == '+') {
                next();
                buf.append('+');
            } else if (ch() == '-') {
                next();
                buf.append('-');
            }
            Digits digits3 = readDigits(buf);
            if (digits3 == Digits.NONE) {
                throw new JSONParseException(line, column, "Exponent must have at least one digit");
            }
        }
        Object value;
        if (keepStrings) {
            value = strSign + buf;
        } else {
            if (isign != 0 && !floating && digits1 == Digits.ONLY_ZERO) {
                value = valueFactory.zeroValue(isign);
            } else {
                String numStr = isign < 0 ? "-" + buf : buf.toString();
                if (floating) {
                    value = valueFactory.floatValue(numStr);
                } else {
                    value = valueFactory.intValue(numStr);
                }
            }
        }
        return new JSONToken(floating ? JSONTokenType.FLOAT : JSONTokenType.INT, null, value, line, column);
    }

    private String parseIdent() {
        StringBuilder buf = new StringBuilder();
        buf.appendCodePoint(ch());
        next();
        while (true) {
            int ch = ch();
            if (Character.isJavaIdentifierPart(ch)) {
                next();
                buf.appendCodePoint(ch);
            } else {
                break;
            }
        }
        return buf.toString();
    }

    private boolean isValue(String ident, String valueText) {
        if (caseInsensitive) {
            return valueText.equalsIgnoreCase(ident);
        } else {
            return valueText.equals(ident);
        }
    }

    public JSONToken nextToken() {
        skipSpaces();
        int ch = ch();
        int line = this.line;
        int column = this.column;
        if (ch < 0)
            return new JSONToken(JSONTokenType.EOF, null, line, column);
        JSONTokenType stype = SYMBOLS.get(ch);
        if (stype != null) {
            next();
            return new JSONToken(stype, null, line, column);
        } else if (ch == '"' || ch == '\'') {
            if (!singleQuotes && ch == '\'') {
                throw new JSONParseException(line, column, "Single quotes are not allowed");
            }
            String string = parseString(ch);
            return new JSONToken(JSONTokenType.STRING, string, line, column);
        } else if ((ch >= '0' && ch <= '9') || ch == '+' || ch == '-' || ch == '.') {
            return parseNumber(line, column);
        } else if (Character.isJavaIdentifierStart(ch)) {
            String ident = parseIdent();
            JSONTokenType type;
            Object value = null;
            if (isValue(ident, "true")) {
                value = keepStrings ? ident : valueFactory.boolValue(true);
                type = JSONTokenType.TRUE;
            } else if (isValue(ident, "false")) {
                value = keepStrings ? ident : valueFactory.boolValue(false);
                type = JSONTokenType.FALSE;
            } else if (isValue(ident, "null")) {
                value = keepStrings ? ident : valueFactory.nullValue();
                type = JSONTokenType.NULL;
            } else if ("NaN".equalsIgnoreCase(ident)) {
                value = keepStrings ? ident : valueFactory.nanValue();
                type = JSONTokenType.IDENT_FLOAT;
            } else if (isInfinity(ident)) {
                value = keepStrings ? ident : valueFactory.infinityValue(0);
                type = JSONTokenType.IDENT_FLOAT;
            } else {
                type = JSONTokenType.IDENT;
            }
            return new JSONToken(type, ident, value, line, column);
        } else {
            String chStr = new String(Character.toChars(ch));
            throw new JSONParseException(line, column, "Unexpected character '" + chStr + "'");
        }
    }
}
