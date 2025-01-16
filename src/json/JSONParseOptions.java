package json;

import java.util.EnumSet;
import java.util.Set;

public class JSONParseOptions {

    public Set<JSONReadFeature> features = EnumSet.noneOf(JSONReadFeature.class);
    public JSONValueFactory valueFactory = new JSONValueFactory();
    public boolean keepStrings = false;
}
