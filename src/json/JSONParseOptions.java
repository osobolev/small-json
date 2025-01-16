package json;

import java.util.Set;

public class JSONParseOptions {

    public final Set<JSONReadFeature> features;
    public final JSONValueFactory valueFactory;

    public JSONParseOptions(Set<JSONReadFeature> features, JSONValueFactory valueFactory) {
        this.features = features;
        this.valueFactory = valueFactory;
    }

    public JSONParseOptions(Set<JSONReadFeature> features) {
        this(features, new JSONValueFactory());
    }
}
