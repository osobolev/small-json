package json;

import java.io.StringReader;

public class CodepointTest {

    public static void main(String[] args) {
        try {
            String json = "[ 1, \"\uD83C\uDF55\", \"\uD83C\" ]";
            JSONParseOptions options = JSONParseOptions.DEFAULT;
            JSONLexer lexer = new JSONLexer(options, new StringReader(json));
            while (true) {
                JSONToken t = lexer.nextToken();
                System.out.println(t);
                if (t.type == JSONTokenType.EOF)
                    break;
            }
        } catch (JSONParseException ex) {
            System.out.println(ex.toString());
            System.out.println(ex.index);
        }
    }
}
