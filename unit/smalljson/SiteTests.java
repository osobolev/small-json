package smalljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SiteTests {

    @Test
    public void runJsonSiteTests() throws IOException {
        JSONParseOptions options = SiteUtil.siteOptions();
        SiteUtil.scanSiteTests((name, failing, is) -> {
            if (failing) {
                assertThrows(
                    JSONParseException.class,
                    () -> new JSONParser(options, is).parse(),
                    name
                );
            } else {
                assertDoesNotThrow(
                    () -> new JSONParser(options, is).parse(),
                    name
                );
            }
        });
    }
}
