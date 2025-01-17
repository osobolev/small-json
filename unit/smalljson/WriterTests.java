package smalljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static smalljson.TestUtil.parse;

public class WriterTests {

    @Test
    public void testWriter() throws IOException {
        JSONParseOptions options = SiteUtil.siteOptions();
        SiteUtil.scanSiteTests((name, failing, is) -> {
            if (failing)
                return;
            Object origObj = new JSONParser(options, is).parse();
            String json1 = JSONWriter.toString(origObj);

            Object newObj = parse(json1);
            assertEquals(origObj, newObj);

            String json2 = JSONWriter.toString(newObj);
            assertEquals(json1, json2);
        });
    }
}
