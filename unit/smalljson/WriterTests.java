package smalljson;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static smalljson.TestUtil.parse;

public class WriterTests {

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    public void testWriter(String indent) throws IOException {
        JSONParseOptions options = SiteUtil.siteOptions();
        SiteUtil.scanSiteTests((name, failing, is) -> {
            if (failing)
                return;
            Object origObj = new JSONParser(options, is).parse();
            String json1 = JSONWriter.toString(origObj, indent);

            Object newObj = parse(json1);
            assertEquals(origObj, newObj);

            String json2 = JSONWriter.toString(newObj, indent);
            assertEquals(json1, json2);
        });
    }
}
