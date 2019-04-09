package se.tink.backend.aggregation.locks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public class BarrierName {

    public enum Prefix {
        SUPPLEMENTAL_INFORMATION("/locks/supplementCredentials/credentials/");

        private final String prefix;

        Prefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        @JsonCreator
        public static Prefix fromKey(String key) {
            for (Prefix value : values()) {
                if (Objects.equal(value.getPrefix(), key)) {
                    return value;
                }
            }

            throw new IllegalArgumentException(String.format("No key found: %s", key));
        }

        /** Need to override the toString() for Jackson to get the key in maps from custom getter */
        @JsonValue
        @Override
        public String toString() {
            return getPrefix();
        }
    }

    public static String build(Prefix prefix, String suffix) {
        return prefix.toString() + suffix;
    }
}
