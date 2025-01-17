package smalljson;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriterTest {

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Downloads\\test\\test\\pass1.json"));
        String json = new String(bytes, StandardCharsets.UTF_8);
//        String json = "\"\\b\\u0123\\u4567\\u89AB\\uCDEF\\uabcd\\uef4A\"";
        JSONParseOptions options = JSONParseOptions
            .builder()
            .feature(JSONReadFeature.JAVA_COMMENTS)
            .build();
        JSONParser parser = new JSONParser(options, json);
        Object value = parser.parse();
        PrintWriter pw = new PrintWriter(System.out);
        new JSONWriter(pw, "\t").write(value);
        pw.flush();
    }
}
