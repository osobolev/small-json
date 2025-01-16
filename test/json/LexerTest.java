package json;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;

public class LexerTest {

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Downloads\\test\\test\\pass1.json"));
        String json = new String(bytes, StandardCharsets.UTF_8);
        JSONParseOptions options = new JSONParseOptions(EnumSet.noneOf(JSONReadFeature.class));
        JSONLexer lexer = new JSONLexer(options, new StringReader(json));
        while (true) {
            JSONToken t = lexer.nextToken();
            System.out.println(t);
            if (t.type == JSONTokenType.EOF)
                break;
        }
    }
}