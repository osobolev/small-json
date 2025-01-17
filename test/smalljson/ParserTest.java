package smalljson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParserTest {

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Downloads\\test\\test\\pass1.json"));
        String json = new String(bytes, StandardCharsets.UTF_8);
        JSONParseOptions options = JSONParseOptions
            .builder()
            .feature(JSONReadFeature.JAVA_COMMENTS)
            .build();
        JSONParser parser = new JSONParser(options, json);
        Object value = parser.parse();
        System.out.println(value);
    }
}
