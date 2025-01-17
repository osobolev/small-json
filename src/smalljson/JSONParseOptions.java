package smalljson;

import java.util.*;

public final class JSONParseOptions {

    public static final JSONParseOptions DEFAULT = builder().build();

    public final Set<JSONReadFeature> features;
    public final JSONValueFactory valueFactory;
    public final boolean allowDuplicateKeys; // todo: move to features
    public final int maxNestingLevel;

    private JSONParseOptions(Set<JSONReadFeature> features,
                             JSONValueFactory valueFactory,
                             boolean allowDuplicateKeys,
                             int maxNestingLevel) {
        this.features = Collections.unmodifiableSet(features);
        this.valueFactory = valueFactory;
        this.allowDuplicateKeys = allowDuplicateKeys;
        this.maxNestingLevel = maxNestingLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Set<JSONReadFeature> features = EnumSet.noneOf(JSONReadFeature.class);
        private JSONValueFactory valueFactory = JSONValueFactory.DEFAULT;
        private boolean allowDuplicateKeys = false;
        private int maxNestingLevel = 512;

        public Builder copy(JSONParseOptions other) {
            this.features(other.features);
            this.valueFactory(other.valueFactory);
            this.allowDuplicateKeys(other.allowDuplicateKeys);
            this.maxNestingLevel(other.maxNestingLevel);
            return this;
        }

        public Builder features(Collection<JSONReadFeature> features) {
            this.features.clear();
            this.features.addAll(features);
            return this;
        }

        public Builder features(JSONReadFeature... features) {
            this.features.addAll(Arrays.asList(features));
            return this;
        }

        public Builder feature(JSONReadFeature feature) {
            this.features.add(feature);
            return this;
        }

        public Builder valueFactory(JSONValueFactory valueFactory) {
            this.valueFactory = valueFactory;
            return this;
        }

        public Builder allowDuplicateKeys(boolean allowDuplicateKeys) {
            this.allowDuplicateKeys = allowDuplicateKeys;
            return this;
        }

        public Builder maxNestingLevel(int maxNestingLevel) {
            this.maxNestingLevel = maxNestingLevel;
            return this;
        }

        public JSONParseOptions build() {
            return new JSONParseOptions(
                features, valueFactory, allowDuplicateKeys, maxNestingLevel
            );
        }
    }
}
