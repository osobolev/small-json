package smalljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SiteTests {

    @Test
    public void runJsonSiteTests() throws IOException {
        JSONFactory options = SiteUtil.siteOptions();
        SiteUtil.scanSiteTests((name, failing, is) -> {
            if (failing) {
                assertThrows(
                    JSONParseException.class,
                    () -> options.parse(is),
                    name
                );
            } else {
                assertDoesNotThrow(
                    () -> options.parse(is),
                    name
                );
            }
        });
    }
}
