package json;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class JSONLexer {

    private final Reader input;
    private final boolean comments;
    private final boolean singleQuotes;
    private final boolean invalidEscapes;
    private final boolean unescapedControls;
    private final boolean specialNumbers;

    private int line = 1;
    private int column = 1;
    private int ch1;
    private int ch2;

    public JSONLexer(JSONParseOptions options, Reader input) throws IOException {
        this.input = input;
        this.comments = options.features.contains(JSONReadFeature.ALLOW_JAVA_COMMENTS);
        this.singleQuotes = options.features.contains(JSONReadFeature.ALLOW_SINGLE_QUOTES);
        this.invalidEscapes = options.features.contains(JSONReadFeature.INVALID_ESCAPES);
        this.unescapedControls = options.features.contains(JSONReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
        this.specialNumbers = options.features.contains(JSONReadFeature.ALLOW_NON_NUMERIC_NUMBERS);

        this.ch1 = nextCodepoint(input);
        this.ch2 = nextCodepoint(input);
    }

    private static int nextCodepoint(Reader input) throws IOException {
        int c1 = input.read();
        if (c1 < 0)
            return -1;
        if (!Character.isHighSurrogate((char) c1))
            return c1;
        int c2 = input.read();
        if (c2 < 0)
            return -1; // todo: throw error???
        // todo: check for low surrogate???
        return Character.toCodePoint((char) c1, (char) c2);
    }

    private int ch() {
        return ch1;
    }

    private void next() throws IOException {
        if (ch1 == '\n') {
            line++;
            column = 1;
        } else if (ch1 >= 0) {
            column++;
        }
        ch1 = ch2;
        ch2 = nextCodepoint(input);
    }

    private boolean match(char c) throws IOException {
        if (ch() == c) {
            next();
            return true;
        } else {
            return false;
        }
    }

    private void skipSpaces() throws IOException {
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
                    while (ch1 >= 0) {
                        if (ch1 == '*' && ch2 == '/') {
                            next();
                            next();
                            break;
                        }
                        next();
                    }
                    continue;
                }
            }
            if (ch > ' ') // todo: other space chars???
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

    private void parseEscape(StringBuilder buf) throws IOException {
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

    private String parseString(int quote) throws IOException {
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

    private int readDigits(StringBuilder buf) throws IOException {
        int digits = 0;
        while (true) {
            int ch = ch();
            if (ch >= '0' && ch <= '9') {
                next();
                buf.appendCodePoint(ch);
                digits++;
            } else {
                break;
            }
        }
        return digits;
    }

    private static boolean isInfinity(String ident) {
        return "Infinity".equalsIgnoreCase(ident) || "inf".equalsIgnoreCase(ident);
    }

    // todo: separate int and double values???
    private JSONToken parseNumber(int line, int column) throws IOException {
        StringBuilder buf = new StringBuilder();
        if (match('+')) {
            // todo: error in strict mode
            buf.append('+');
        } else if (match('-')) {
            buf.append('-');
        }
        if (specialNumbers) {
            int ch = ch();
            if (ch >= 0 && Character.isJavaIdentifierStart(ch)) {
                String ident = parseIdent();
                if (isInfinity(ident)) {
                    double numberValue = buf.charAt(0) == '-' ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                    return new JSONToken(JSONTokenType.FLOAT, null, numberValue, line, column);
                } else {
                    throw new JSONParseException(line, column, "Invalid infinite number");
                }
            }
        }
        int digits1 = readDigits(buf);
        int digits = digits1;
        if (match('.')) {
            buf.append('.');
            int digits2 = readDigits(buf);
            digits += digits2;
        }
        if (digits == 0) {
            // todo: error
        }
        if (match('e') || match('E')) {
            buf.append('e'); // todo: preserve original case???
            if (match('+')) {
                buf.append('+');
            } else if (match('-')) {
                buf.append('-');
            }
            int digits3 = readDigits(buf);
            if (digits3 == 0) {
                // todo: error
            }
        }
        Object numberValue = null; // todo!!! parse as ???
        // todo: check strict JSON syntax for numbers
        return new JSONToken(JSONTokenType.FLOAT, null, numberValue, line, column);
    }

    private String parseIdent() throws IOException {
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

    private JSONToken tryNextToken() throws IOException {
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
            Object numberValue = null;
            // todo: exact match in strict mode for true/false/null:
            if ("true".equalsIgnoreCase(ident)) {
                type = JSONTokenType.TRUE;
            } else if ("false".equalsIgnoreCase(ident)) {
                type = JSONTokenType.FALSE;
            } else if ("null".equalsIgnoreCase(ident)) {
                type = JSONTokenType.NULL;
            } else if ("NaN".equalsIgnoreCase(ident)) {
                numberValue = Double.NaN;
                type = JSONTokenType.IDENT_FLOAT;
            } else if (isInfinity(ident)) {
                numberValue = Double.POSITIVE_INFINITY;
                type = JSONTokenType.IDENT_FLOAT;
            } else {
                type = JSONTokenType.IDENT;
            }
            return new JSONToken(type, ident, numberValue, line, column);
        } else {
            return null;
        }
    }

    public JSONToken nextToken() throws IOException {
        while (true) {
            JSONToken token = tryNextToken();
            if (token == null) {
                // todo: error in strict mode
                next();
            } else {
                return token;
            }
        }
    }
}
