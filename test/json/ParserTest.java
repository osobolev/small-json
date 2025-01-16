package json;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;

public class ParserTest {

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Downloads\\test\\test\\pass1.json"));
        String json = new String(bytes, StandardCharsets.UTF_8);
        EnumSet<JSONReadFeature> features = EnumSet.noneOf(JSONReadFeature.class);
        features.add(JSONReadFeature.JAVA_COMMENTS);
        JSONParseOptions options = new JSONParseOptions();
        options.features = features;
        JSONLexer lexer = new JSONLexer(options, new StringReader(json));
        JSONParser parser = new JSONParser(options, lexer);
        Object value = parser.parse();
        System.out.println(value);
    }
}
