package json;

import java.io.StringReader;

public class NestingTest {

    public static void main(String[] args) {
        String json = "[[1]]";
        JSONParseOptions options = new JSONParseOptions();
        options.maxNestingLevel = 1;
        JSONParser parser = new JSONParser(options, new StringReader(json));
        Object value = parser.parse();
        System.out.println(value);
    }
}
