package json;

import java.util.Set;

public class JSONParseOptions {

    public final Set<JSONReadFeature> features;

    public JSONParseOptions(Set<JSONReadFeature> features) {
        this.features = features;
    }
}
