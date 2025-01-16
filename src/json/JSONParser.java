package json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JSONParser {

    private final JSONLexer lexer;
    private final JSONParseOptions options;
    private final boolean specialNumbers;
    private final boolean allowMissingValues;
    private final boolean allowTrailingComma;
    private final boolean unquotedFields;

    private JSONToken current;

    public JSONParser(JSONParseOptions options, JSONLexer lexer) {
        this.lexer = lexer;
        this.options = options;
        this.specialNumbers = options.features.contains(JSONReadFeature.NAN_INF_NUMBERS);
        this.allowMissingValues = options.features.contains(JSONReadFeature.ARRAY_MISSING_VALUES);
        this.allowTrailingComma = options.features.contains(JSONReadFeature.TRAILING_COMMA);
        this.unquotedFields = options.features.contains(JSONReadFeature.UNQUOTED_FIELD_NAMES);

        this.current = lexer.nextToken();
    }

    public JSONParser(JSONParseOptions options, Reader rdr) {
        this(options, new JSONLexer(options, rdr));
    }

    private void next() {
        current = lexer.nextToken();
    }

    private void require(JSONTokenType type, String message) {
        if (current.type == type) {
            next();
        } else {
            throw new JSONParseException(current, message + ", but " + current.type + " found");
        }
    }

    private enum PrevState {
        START, COMMA, VALUE
    }

    // todo: control nesting level
    public Map<String, Object> parseObject() {
        require(JSONTokenType.LCURLY, "Object must start with '{'");
        Map<String, Object> object = new LinkedHashMap<>();
        PrevState prev = PrevState.START;
        while (true) {
            JSONTokenType type = current.type;
            if (type == JSONTokenType.COMMA) {
                if (prev != PrevState.VALUE) {
                    throw new JSONParseException(current, "Extra comma in object");
                }
                next();
                prev = PrevState.COMMA;
            } else if (type == JSONTokenType.RCURLY) {
                if (prev == PrevState.COMMA) {
                    if (!allowTrailingComma) {
                        throw new JSONParseException(current, "Trailing comma in object");
                    }
                }
                next();
                break;
            } else {
                if (prev == PrevState.VALUE) {
                    throw new JSONParseException(current, "Missing comma in object");
                }
                String key;
                if (type == JSONTokenType.STRING || type == JSONTokenType.IDENT || type == JSONTokenType.IDENT_FLOAT) {
                    if (!unquotedFields && type != JSONTokenType.STRING) {
                        throw new JSONParseException(current, "Unquoted field names are not allowed");
                    }
                    key = current.text;
                    if (!options.allowDuplicateKeys && object.containsKey(key)) {
                        throw new JSONParseException(current, "Duplicate key '" + key + "' in object");
                    }
                    next();
                } else {
                    throw new JSONParseException(current, "Expected field name but found " + type);
                }
                require(JSONTokenType.COLON, "Expected colon after key");
                Object value = parse();
                object.put(key, value);
                prev = PrevState.VALUE;
            }
        }
        return object;
    }

    public List<Object> parseArray() {
        require(JSONTokenType.LSQUARE, "Array must start with '['");
        List<Object> array = new ArrayList<>();
        PrevState prev = PrevState.START;
        while (true) {
            JSONTokenType type = current.type;
            if (type == JSONTokenType.COMMA) {
                if (prev != PrevState.VALUE) {
                    if (allowMissingValues) {
                        array.add(options.valueFactory.nullObject());
                    } else {
                        throw new JSONParseException(current, "Extra comma in array");
                    }
                }
                next();
                prev = PrevState.COMMA;
            } else if (type == JSONTokenType.RSQUARE) {
                if (prev == PrevState.COMMA) {
                    if (allowTrailingComma) {
                        // do nothing
                    } else if (allowMissingValues) {
                        array.add(options.valueFactory.nullObject());
                    } else {
                        throw new JSONParseException(current, "Trailing comma in array");
                    }
                }
                next();
                break;
            } else {
                if (prev == PrevState.VALUE) {
                    throw new JSONParseException(current, "Missing comma in array");
                }
                Object value = parse();
                array.add(value);
                prev = PrevState.VALUE;
            }
        }
        return array;
    }

    public Object parsePrimitive() {
        JSONTokenType type = current.type;
        Object result;
        if (type == JSONTokenType.STRING) {
            result = current.text;
        } else if (type == JSONTokenType.FLOAT || type == JSONTokenType.INT) {
            result = current.value;
        } else if (type == JSONTokenType.IDENT_FLOAT) {
            if (!specialNumbers) {
                throw new JSONParseException(current, "Use of non-numeric floating point numbers not allowed");
            }
            result = current.value;
        } else if (type == JSONTokenType.NULL || type == JSONTokenType.TRUE || type == JSONTokenType.FALSE) {
            result = current.value;
        } else {
            throw new JSONParseException(current, "Unexpected token " + current.type);
        }
        next();
        return result;
    }

    public Object parse() {
        JSONTokenType type = current.type;
        if (type == JSONTokenType.LCURLY) {
            return parseObject();
        } else if (type == JSONTokenType.LSQUARE) {
            return parseArray();
        } else {
            return parsePrimitive();
        }
    }
}
