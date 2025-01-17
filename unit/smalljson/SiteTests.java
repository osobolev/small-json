package smalljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static smalljson.TestUtil.options;

public class SiteTests {

    @Test
    public void runJsonSiteTests() throws IOException {
        JSONParseOptions options = JSONParseOptions.builder().copy(options()).maxNestingLevel(19).build();
        try (InputStream is = getClass().getResourceAsStream("/test.zip");) {
            assertNotNull(is);
            try (ZipInputStream zis = new ZipInputStream(is)) {
                while (true) {
                    ZipEntry entry = zis.getNextEntry();
                    if (entry == null)
                        break;
                    if (entry.isDirectory())
                        continue;
                    String name = entry.getName();
                    int p = name.lastIndexOf('/');
                    String fileName = name.substring(p + 1);
                    if (fileName.startsWith("fail") && !"fail1.json".equals(fileName)) {
                        assertThrows(
                            JSONParseException.class,
                            () -> new JSONParser(options, zis).parse(),
                            name
                        );
                    } else {
                        new JSONParser(options, zis).parse();
                    }
                }
            }
        }
    }
}
