package smalljson.parser;

import smalljson.*;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public final class JSONParser {

    private final JSONLexer lexer;
    private final JSONParseOptions options;
    private final boolean specialNumbers;
    private final boolean allowMissingValues;
    private final boolean allowTrailingComma;
    private final boolean unquotedFields;
    private final boolean duplicateKeys;
    private final boolean extraChars;

    private JSONToken current;

    public JSONParser(JSONParseOptions options, Reader rdr) {
        Reader input = rdr.markSupported() ? rdr : new BufferedReader(rdr);
        this.lexer = new JSONLexer(options, input);
        this.options = options;
        this.specialNumbers = options.features.contains(JSONFeature.NAN_INF_NUMBERS);
        this.allowMissingValues = options.features.contains(JSONFeature.ARRAY_MISSING_VALUES);
        this.allowTrailingComma = options.features.contains(JSONFeature.TRAILING_COMMA);
        this.unquotedFields = options.features.contains(JSONFeature.UNQUOTED_FIELD_NAMES);
        this.duplicateKeys = options.features.contains(JSONFeature.DUPLICATE_FIELD_NAMES);
        this.extraChars = options.features.contains(JSONFeature.EXTRA_CHARS);

        this.current = lexer.nextToken();
    }

    public void next() {
        current = lexer.nextToken();
    }

    public JSONToken getCurrent() {
        return current;
    }

    private void require(JSONTokenType type, String message) {
        if (current.type == type) {
            next();
        } else {
            throw new JSONParseException(current, message + ", but " + current.type + " found");
        }
    }

    private void checkNestingLevel(int nestingLevel) {
        if (options.maxNestingLevel >= 0 && nestingLevel > options.maxNestingLevel) {
            throw new JSONParseException(current, "Maximum nesting level " + options.maxNestingLevel + " reached");
        }
    }

    private enum PrevState {
        START, COMMA, VALUE
    }

    private JSONObject parseObject(int nestingLevel) {
        checkNestingLevel(nestingLevel);
        require(JSONTokenType.LCURLY, "Object must start with '{'");
        Map<String, Object> object = options.valueFactory.objectValue();
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
                if (type == JSONTokenType.STRING || type == JSONTokenType.IDENT ||
                    type == JSONTokenType.TRUE || type == JSONTokenType.FALSE || type == JSONTokenType.NULL ||
                    type == JSONTokenType.IDENT_FLOAT) {
                    if (!unquotedFields && type != JSONTokenType.STRING) {
                        throw new JSONParseException(current, "Unquoted field names are not allowed");
                    }
                    key = current.text;
                    if (!duplicateKeys && object.containsKey(key)) {
                        throw new JSONParseException(current, "Duplicate key '" + key + "' in object");
                    }
                    next();
                } else {
                    throw new JSONParseException(current, "Expected field name but found " + type);
                }
                require(JSONTokenType.COLON, "Expected colon after key");
                Object value = parse(nestingLevel);
                object.put(key, value);
                prev = PrevState.VALUE;
            }
        }
        return new JSONObject(object);
    }

    public JSONObject parseObject() {
        JSONObject result = parseObject(1);
        checkEOF();
        return result;
    }

    private JSONArray parseArray(int nestingLevel) {
        checkNestingLevel(nestingLevel);
        require(JSONTokenType.LSQUARE, "Array must start with '['");
        List<Object> array = options.valueFactory.arrayValue();
        PrevState prev = PrevState.START;
        while (true) {
            JSONTokenType type = current.type;
            if (type == JSONTokenType.COMMA) {
                if (prev != PrevState.VALUE) {
                    if (allowMissingValues) {
                        array.add(options.valueFactory.nullValue());
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
                        array.add(options.valueFactory.nullValue());
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
                Object value = parse(nestingLevel);
                array.add(value);
                prev = PrevState.VALUE;
            }
        }
        return new JSONArray(array);
    }

    public JSONArray parseArray() {
        JSONArray result = parseArray(1);
        checkEOF();
        return result;
    }

    public Object parsePrimitive() {
        JSONTokenType type = current.type;
        Object result;
        if (type == JSONTokenType.STRING) {
            result = current.text;
        } else if (type == JSONTokenType.FLOAT || type == JSONTokenType.INT) {
            result = current.value;
        } else if (type == JSONTokenType.NULL || type == JSONTokenType.TRUE || type == JSONTokenType.FALSE) {
            result = current.value;
        } else if (type == JSONTokenType.IDENT_FLOAT) {
            if (!specialNumbers) {
                throw new JSONParseException(current, "Use of non-numeric floating point numbers not allowed");
            }
            result = current.value;
        } else {
            throw new JSONParseException(current, "Unexpected token " + current.type);
        }
        next();
        return result;
    }

    private Object parse(int nestingLevel) {
        JSONTokenType type = current.type;
        if (type == JSONTokenType.LCURLY) {
            return parseObject(nestingLevel + 1);
        } else if (type == JSONTokenType.LSQUARE) {
            return parseArray(nestingLevel + 1);
        } else {
            return parsePrimitive();
        }
    }

    public Object parse() {
        Object result = parse(0);
        checkEOF();
        return result;
    }

    public void checkEOF() {
        if (!extraChars && current.type != JSONTokenType.EOF) {
            throw new JSONParseException(current, "Extra character at the end");
        }
    }
}
