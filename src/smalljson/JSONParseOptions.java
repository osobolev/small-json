package smalljson;

import java.util.*;

public final class JSONParseOptions {

    public static final JSONParseOptions DEFAULT = builder().buildOptions();

    public final Set<JSONReadFeature> features;
    public final JSONValueFactory valueFactory;
    public final int maxNestingLevel;

    private JSONParseOptions(Set<JSONReadFeature> features,
                             JSONValueFactory valueFactory,
                             int maxNestingLevel) {
        this.features = Collections.unmodifiableSet(features);
        this.valueFactory = valueFactory;
        this.maxNestingLevel = maxNestingLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Set<JSONReadFeature> features = EnumSet.noneOf(JSONReadFeature.class);
        private JSONValueFactory valueFactory = JSONValueFactory.DEFAULT;
        private int maxNestingLevel = 512;

        public Builder copy(JSONParseOptions other) {
            this.setFeatures(other.features);
            this.valueFactory(other.valueFactory);
            this.maxNestingLevel(other.maxNestingLevel);
            return this;
        }

        /**
         * Replaces existing features
         */
        public Builder setFeatures(Collection<JSONReadFeature> features) {
            this.features.clear();
            this.features.addAll(features);
            return this;
        }

        /**
         * Adds new features
         */
        public Builder addFeatures(Collection<JSONReadFeature> features) {
            this.features.addAll(features);
            return this;
        }

        /**
         * Adds new features
         */
        public Builder addFeatures(JSONReadFeature... features) {
            addFeatures(Arrays.asList(features));
            return this;
        }

        /**
         * Adds new feature
         */
        public Builder feature(JSONReadFeature feature) {
            this.features.add(feature);
            return this;
        }

        public Builder valueFactory(JSONValueFactory valueFactory) {
            this.valueFactory = valueFactory;
            return this;
        }

        public Builder maxNestingLevel(int maxNestingLevel) {
            this.maxNestingLevel = maxNestingLevel;
            return this;
        }

        public JSONParseOptions buildOptions() {
            return new JSONParseOptions(features, valueFactory, maxNestingLevel);
        }

        public JSON build() {
            return new JSON(buildOptions());
        }
    }
}
