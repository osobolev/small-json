package smalljson;

import smalljson.parser.FastBufferedReader;
import smalljson.parser.FastReader;
import smalljson.parser.FastStringReader;
import smalljson.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JSONFactory {

    public static final JSONFactory JSON = new JSONFactory();

    private final JSONParseOptions options;

    public JSONFactory(JSONParseOptions options) {
        this.options = options;
    }

    public JSONFactory() {
        this(JSONParseOptions.DEFAULT);
    }

    public JSONParseOptions getOptions() {
        return options;
    }

    public static JSONParseOptions.Builder options() {
        return JSONParseOptions.builder();
    }

    public JSONObject newObject() {
        return new JSONObject(options.valueFactory.objectValue());
    }

    public JSONArray newArray() {
        return new JSONArray(options.valueFactory.arrayValue());
    }

    // Parsing

    public JSONParser newParser(FastReader rdr) {
        return new JSONParser(options, rdr);
    }

    public static FastReader toFast(Reader rdr) {
        return new FastBufferedReader(rdr);
    }

    public static FastReader toFast(InputStream is) {
        return toFast(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public static FastReader toFast(String json) {
        return new FastStringReader(json);
    }

    // Objects

    public JSONObject parseObject(FastReader rdr) {
        return newParser(rdr).parseObject();
    }

    public JSONObject parseObject(Reader rdr) {
        return parseObject(toFast(rdr));
    }

    public JSONObject parseObject(InputStream is) {
        return parseObject(toFast(is));
    }

    public JSONObject parseObject(String json) {
        return parseObject(toFast(json));
    }

    public JSONObject parseObject(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parseObject(rdr);
        }
    }

    // Arrays

    public JSONArray parseArray(FastReader rdr) {
        return newParser(rdr).parseArray();
    }

    public JSONArray parseArray(Reader rdr) {
        return parseArray(toFast(rdr));
    }

    public JSONArray parseArray(InputStream is) {
        return parseArray(toFast(is));
    }

    public JSONArray parseArray(String json) {
        return parseArray(toFast(json));
    }

    public JSONArray parseArray(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parseArray(rdr);
        }
    }

    // Any value

    public Object parse(FastReader rdr) {
        return newParser(rdr).parse();
    }

    public Object parse(Reader rdr) {
        return parse(toFast(rdr));
    }

    public Object parse(InputStream is) {
        return parse(toFast(is));
    }

    public Object parse(String json) {
        return parse(toFast(json));
    }

    public Object parse(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parse(rdr);
        }
    }
}
