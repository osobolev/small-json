package json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JSONParser {

    private static final Object NULL = null; // todo

    private final JSONParseOptions options;
    private final JSONLexer lexer;
    private final boolean allowMissingValues;
    private final boolean allowTrailingComma;

    private JSONToken current;

    public JSONParser(JSONParseOptions options, JSONLexer lexer) throws IOException {
        this.options = options;
        this.lexer = lexer;
        this.allowMissingValues = options.features.contains(JSONReadFeature.ALLOW_MISSING_VALUES);
        this.allowTrailingComma = options.features.contains(JSONReadFeature.ALLOW_TRAILING_COMMA);
        this.current = lexer.nextToken();
    }

    private void next() throws IOException {
        current = lexer.nextToken();
    }

    private boolean match(JSONTokenType type) throws IOException {
        if (current.type == type) {
            next();
            return true;
        } else {
            return false;
        }
    }

    private JSONToken require(JSONTokenType type) throws IOException {
        if (current.type == type) {
            JSONToken matches = current;
            next();
            return matches;
        } else {
            throw new JSONParseException(current, "Expected " + type + " but found " + current.type);
        }
    }

    public Object parse() throws IOException {
        JSONTokenType type = current.type;
        if (type == JSONTokenType.LCURLY) {
            next();
            Map<String, Object> object = new LinkedHashMap<>();
            if (!match(JSONTokenType.RCURLY)) {
                while (true) {
                    String key = require(JSONTokenType.STRING).text;
                    require(JSONTokenType.COLON);
                    Object value = parse();
                    object.put(key, value);
                    if (match(JSONTokenType.RCURLY))
                        break;
                    require(JSONTokenType.COMMA);
                    if (allowTrailingComma && match(JSONTokenType.RCURLY))
                        break;
                }
            }
            return object;
        } else if (type == JSONTokenType.LSQUARE) {
            next();
            List<Object> array = new ArrayList<>();
            if (!match(JSONTokenType.RSQUARE)) {
                while (true) {
                    Object value = parse();
                    array.add(value);
                    if (match(JSONTokenType.RSQUARE))
                        break;
                    require(JSONTokenType.COMMA);
                    // todo: handle allowMissingValues
                    if (allowTrailingComma && match(JSONTokenType.RSQUARE))
                        break;
                }
            }
            return array;
        } else if (type == JSONTokenType.STRING) {
            String string = current.text;
            next();
            return string;
        } else if (type == JSONTokenType.NUMBER) {
            String number = current.text;
            next();
            return Double.parseDouble(number); // todo!!!
        } else if (type == JSONTokenType.NULL) {
            next();
            return NULL;
        } else if (type == JSONTokenType.TRUE) {
            next();
            return Boolean.TRUE;
        } else if (type == JSONTokenType.FALSE) {
            next();
            return Boolean.FALSE;
        } else {
            throw new JSONParseException(current, "Unexpected token " + current.type);
        }
    }
}
