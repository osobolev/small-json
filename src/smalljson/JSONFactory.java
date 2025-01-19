package smalljson;

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

    public JSONParser newParser(Reader rdr) {
        return new JSONParser(options, rdr);
    }

    public static Reader toReader(InputStream is) {
        return new InputStreamReader(is, StandardCharsets.UTF_8);
    }

    // Objects

    public JSONObject parseObject(Reader rdr) {
        return newParser(rdr).parseObject();
    }

    public JSONObject parseObject(InputStream is) {
        return parseObject(toReader(is));
    }

    public JSONObject parseObject(String json) {
        return parseObject(new StringReader(json));
    }

    public JSONObject parseObject(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parseObject(rdr);
        }
    }

    // Arrays

    public JSONArray parseArray(Reader rdr) {
        return newParser(rdr).parseArray();
    }

    public JSONArray parseArray(InputStream is) {
        return parseArray(toReader(is));
    }

    public JSONArray parseArray(String json) {
        return parseArray(new StringReader(json));
    }

    public JSONArray parseArray(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parseArray(rdr);
        }
    }

    // Any value

    public Object parse(Reader rdr) {
        return newParser(rdr).parse();
    }

    public Object parse(InputStream is) {
        return parse(toReader(is));
    }

    public Object parse(String json) {
        return parse(new StringReader(json));
    }

    public Object parse(Path file) throws IOException {
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            return parse(rdr);
        }
    }
}
