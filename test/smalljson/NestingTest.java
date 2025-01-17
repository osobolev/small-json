package smalljson;

public class NestingTest {

    public static void main(String[] args) {
        String json = "[[1]]";
        JSONParseOptions options = JSONParseOptions
            .builder()
            .maxNestingLevel(1)
            .build();
        JSONParser parser = new JSONParser(options, json);
        Object value = parser.parse();
        System.out.println(value);
    }
}
