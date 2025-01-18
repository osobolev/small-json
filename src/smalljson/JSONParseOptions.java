package smalljson;

import java.util.*;

public final class JSONParseOptions {

    public static final JSONParseOptions DEFAULT = builder().buildOptions();

    public final Set<JSONFeature> features;
    public final JSONValueFactory valueFactory;
    public final int maxNestingLevel;

    private JSONParseOptions(Set<JSONFeature> features,
                             JSONValueFactory valueFactory,
                             int maxNestingLevel) {
        this.features = Collections.unmodifiableSet(features);
        this.valueFactory = valueFactory;
        this.maxNestingLevel = maxNestingLevel;
    }

    public Builder copy() {
        return new Builder(features, valueFactory, maxNestingLevel);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Set<JSONFeature> features = EnumSet.noneOf(JSONFeature.class);
        private JSONValueFactory valueFactory;
        private int maxNestingLevel;

        public Builder() {
            valueFactory = JSONValueFactory.DEFAULT;
            maxNestingLevel = 512;
        }

        public Builder(Set<JSONFeature> features, JSONValueFactory valueFactory, int maxNestingLevel) {
            this.features.addAll(features);
            this.valueFactory = valueFactory;
            this.maxNestingLevel = maxNestingLevel;
        }

        /**
         * Replaces existing features
         */
        public Builder setFeatures(Collection<JSONFeature> features) {
            this.features.clear();
            this.features.addAll(features);
            return this;
        }

        /**
         * Adds new features
         */
        public Builder addFeatures(Collection<JSONFeature> features) {
            this.features.addAll(features);
            return this;
        }

        /**
         * Adds new features
         */
        public Builder addFeatures(JSONFeature... features) {
            addFeatures(Arrays.asList(features));
            return this;
        }

        /**
         * Adds new feature
         */
        public Builder feature(JSONFeature feature) {
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
