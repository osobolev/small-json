package smalljson;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static smalljson.TestUtil.options;

public class SiteUtil {

    public interface TestConsumer {

        void consume(String name, boolean failing, InputStream is);
    }

    public static JSONParseOptions siteOptions() {
        return JSONParseOptions.builder().copy(options()).maxNestingLevel(19).build();
    }

    public static void scanSiteTests(TestConsumer consumer) throws IOException {
        try (InputStream is = SiteUtil.class.getResourceAsStream("/test.zip");) {
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
                    boolean failing = fileName.startsWith("fail") && !"fail1.json".equals(fileName);
                    consumer.consume(name, failing, zis);
                }
            }
        }
    }
}
