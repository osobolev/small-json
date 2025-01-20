package smalljson.parser;

import smalljson.JSONFeature;
import smalljson.JSONParseException;
import smalljson.JSONParseOptions;
import smalljson.JSONValueFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

public final class JSONLexer {

    private final Reader input;
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

    private long index = 0;
    private int line = 1;
    private int column = 1;
    private int ch0 = -1;
    private int ch1 = -1;
    private int ch2 = -1;

    public JSONLexer(JSONParseOptions options, Reader input) {
        this.input = input.markSupported() ? input : new BufferedReader(input);
        this.valueFactory = options.valueFactory;
        this.comments = options.features.contains(JSONFeature.JAVA_COMMENTS);
        this.singleQuotes = options.features.contains(JSONFeature.SINGLE_QUOTES);
        this.invalidEscapes = options.features.contains(JSONFeature.INVALID_ESCAPES);
        this.unescapedControls = options.features.contains(JSONFeature.STRING_CONTROL_CHARS);
        this.caseInsensitive = options.features.contains(JSONFeature.CASE_INSENSITIVE);
        this.specialNumbers = options.features.contains(JSONFeature.NAN_INF_NUMBERS);
        this.leadingPlus = options.features.contains(JSONFeature.LEADING_PLUS_SIGN);
        this.leadingZeros = options.features.contains(JSONFeature.LEADING_ZEROS);
        this.leadingPoint = options.features.contains(JSONFeature.LEADING_DECIMAL_POINT);
        this.trailingPoint = options.features.contains(JSONFeature.TRAILING_DECIMAL_POINT);

        this.ch1 = nextCodepoint();
        this.ch2 = nextCodepoint();
    }

    private void moveLocation() {
        if (ch1 < 0)
            return;
        index += ch1 >= Character.MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1;
        if (ch1 == '\r' || ch1 == '\n') {
            if (!(ch0 == '\r' && ch1 == '\n')) {
                line++;
                column = 1;
            }
        } else {
            column++;
        }
    }

    private int nextCodepoint() {
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
            moveLocation();
            throw new JSONParseException(index, line, column, error);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int ch() {
        return ch1;
    }

    private void next() {
        moveLocation();
        ch0 = ch1;
        ch1 = ch2;
        ch2 = nextCodepoint();
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
                        if (ch1 == '\r' || ch1 == '\n') {
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
                        throw new JSONParseException(index, line, column, "Comment is not closed");
                    }
                    continue;
                }
            }
            if (ch > ' ')
                break;
            next();
        }
    }

    private void parseEscape(StringBuilder buf) {
        int ch = ch();
        if (ch < 0) {
            throw new JSONParseException(index, line, column, "Unterminated escape sequence");
        }
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
            long index = this.index;
            int line = this.line;
            int column = this.column;
            int ndigits = 0;
            int unicode = 0;
            while (ndigits < 4) {
                int uch = ch();
                int digit;
                if (uch >= '0' && uch <= '9') {
                    digit = uch - '0';
                } else if (uch >= 'a' && uch <= 'f') {
                    digit = uch - 'a' + 10;
                } else if (uch >= 'A' && uch <= 'F') {
                    digit = uch - 'A' + 10;
                } else {
                    break;
                }
                next();
                ndigits++;
                unicode = (unicode << 4) + digit;
            }
            if (ndigits != 4 && (ndigits == 0 || !invalidEscapes)) {
                throw new JSONParseException(index, line, column, "Invalid unicode escape sequence");
            }
            buf.append((char) unicode);
            return;
        } else {
            if (!invalidEscapes) {
                throw new JSONParseException(index, line, column, "Invalid escape sequence");
            }
            escape = ch;
        }
        next();
        buf.appendCodePoint(escape);
    }

    private JSONToken parseString(long index, int line, int column, int quote) {
        next();
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = ch();
            if (ch < 0) {
                throw new JSONParseException(index, line, column, "String is not terminated");
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
                throw new JSONParseException(index, line, column, "Non-escaped control character");
            }
            next();
            buf.appendCodePoint(ch);
        }
        return new JSONToken(JSONTokenType.STRING, buf.toString(), index, line, column);
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

    private JSONToken parseNumber(long index, int line, int column) {
        int isign = 0;
        if (ch() == '+') {
            if (!leadingPlus) {
                throw new JSONParseException(index, line, column, "Plus sign is not allowed");
            }
            next();
            isign = 1;
        } else if (ch() == '-') {
            next();
            isign = -1;
        }
        if (specialNumbers) {
            int ch = ch();
            if (Character.isJavaIdentifierStart(ch)) {
                String ident = parseIdent();
                if (isInfinity(ident)) {
                    Object value = valueFactory.infinityValue(isign);
                    return new JSONToken(JSONTokenType.FLOAT, null, value, index, line, column);
                } else {
                    throw new JSONParseException(index, line, column, "Invalid infinite number");
                }
            }
        }
        StringBuilder buf = new StringBuilder();
        Digits digits1 = readDigits(buf);
        if (!leadingPoint && digits1 == Digits.NONE) {
            throw new JSONParseException(index, line, column, "Leading decimal point is not allowed");
        }
        if (!leadingZeros && buf.length() > 1 && buf.charAt(0) == '0') {
            throw new JSONParseException(index, line, column, "Leading zeros are not allowed");
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
                    throw new JSONParseException(index, line, column, "Trailing decimal point is not allowed");
                }
            } else {
                hasDigits = true;
            }
        }
        if (!hasDigits) {
            throw new JSONParseException(index, line, column, "Number must have at least one digit");
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
                throw new JSONParseException(index, line, column, "Exponent must have at least one digit");
            }
        }
        Object value;
        if (isign != 0 && !floating && digits1 == Digits.ONLY_ZERO) {
            value = valueFactory.zeroValue(isign);
        } else {
            String absNum = buf.toString();
            if (floating) {
                value = valueFactory.floatValue(isign < 0 ? "-" + absNum : absNum);
            } else {
                value = valueFactory.intValue(isign < 0 ? -1 : 1, absNum);
            }
        }
        return new JSONToken(floating ? JSONTokenType.FLOAT : JSONTokenType.INT, null, value, index, line, column);
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
        long index = this.index;
        int line = this.line;
        int column = this.column;
        if (ch < 0)
            return new JSONToken(JSONTokenType.EOF, null, index, line, column);
        JSONTokenType stype;
        if (ch == '{') {
            stype = JSONTokenType.LCURLY;
        } else if (ch == '}') {
            stype = JSONTokenType.RCURLY;
        } else if (ch == '[') {
            stype = JSONTokenType.LSQUARE;
        } else if (ch == ']') {
            stype = JSONTokenType.RSQUARE;
        } else if (ch == ',') {
            stype = JSONTokenType.COMMA;
        } else if (ch == ':') {
            stype = JSONTokenType.COLON;
        } else {
            stype = null;
        }
        if (stype != null) {
            next();
            return new JSONToken(stype, null, index, line, column);
        } else if (ch == '"' || ch == '\'') {
            if (!singleQuotes && ch == '\'') {
                throw new JSONParseException(index, line, column, "Single quotes are not allowed");
            }
            return parseString(index, line, column, ch);
        } else if ((ch >= '0' && ch <= '9') || ch == '+' || ch == '-' || ch == '.') {
            return parseNumber(index, line, column);
        } else if (Character.isJavaIdentifierStart(ch)) {
            String ident = parseIdent();
            JSONTokenType type;
            Object value = null;
            if (isValue(ident, "true")) {
                value = valueFactory.boolValue(true);
                type = JSONTokenType.TRUE;
            } else if (isValue(ident, "false")) {
                value = valueFactory.boolValue(false);
                type = JSONTokenType.FALSE;
            } else if (isValue(ident, "null")) {
                value = valueFactory.nullValue();
                type = JSONTokenType.NULL;
            } else if ("NaN".equalsIgnoreCase(ident)) {
                value = valueFactory.nanValue();
                type = JSONTokenType.IDENT_FLOAT;
            } else if (isInfinity(ident)) {
                value = valueFactory.infinityValue(0);
                type = JSONTokenType.IDENT_FLOAT;
            } else {
                type = JSONTokenType.IDENT;
            }
            return new JSONToken(type, ident, value, index, line, column);
        } else {
            String chStr = new String(Character.toChars(ch));
            throw new JSONParseException(index, line, column, "Unexpected character '" + chStr + "'");
        }
    }
}
