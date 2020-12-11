package se.tink.backend.integration.agent_data_availability_tracker.common.serialization;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrackingList {
    private final List<FieldEntry> fields;

    private TrackingList(List<FieldEntry> fields) {
        this.fields = fields;
    }

    static TrackingList.Builder builder(String keyBase) {
        return new TrackingList.Builder(keyBase);
    }

    public void addAll(final TrackingList other) {
        this.fields.addAll(other.getFields());
    }

    public List<FieldEntry> getFields() {
        return fields;
    }

    public static class Builder {

        public static final String VALUE_NOT_LISTED = "VALUE_NOT_LISTED";

        private final String keyBase;
        private final List<FieldEntry> fields;

        private Builder(String keyBase) {
            this.keyBase = keyBase;
            fields = new ArrayList<>();
        }

        /**
         * Puts the key/value pair in the map, preserving the actual value. This should only be done
         * with non-sensitive and enumerable values.
         *
         * <p>For sensitive or innumerable values use {@link #putRedacted(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Non-sensitive and enumerable value of the field.
         */
        public Builder putListed(@Nonnull String key, @Nullable String value) {
            put(key, value);
            return this;
        }

        /**
         * Puts the key/value pair in the map, preserving the actual value. This should only be done
         * with non-sensitive and enumerable values.
         *
         * <p>For sensitive or innumerable values use {@link #putRedacted(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Non-sensitive and enumerable value of the field.
         */
        public Builder putListed(@Nonnull String key, @Nullable Boolean value) {
            put(key, String.valueOf(value));
            return this;
        }

        /**
         * Puts the key/value pair in the map, preserving the actual value. This should only be done
         * with non-sensitive and enumerable values.
         *
         * <p>For sensitive or innumerable values use {@link #putRedacted(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Non-sensitive and enumerable value of the field.
         */
        public <E extends Enum<E>> Builder putListed(@Nonnull String key, @Nullable E value) {
            put(key, String.valueOf(value));
            return this;
        }

        /**
         * Puts the key/value pair in the map, redacting the value and only recording if the value
         * is null or not.
         *
         * <p>For non-sensitive and enumerable values use {@link #putListed(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Sensitive or innumerable value of the field.
         */
        public Builder putRedacted(@Nonnull String key, @Nullable String value) {
            put(key, toSecret(value));
            return this;
        }

        /**
         * Puts the key/value pair in the map, redacting the value and only recording if the value
         * is null or not.
         *
         * <p>For non-sensitive and enumerable values use {@link #putListed(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Sensitive or innumerable value of the field.
         */
        public Builder putRedacted(@Nonnull String key, @Nullable Number value) {
            put(key, toSecret(value));
            return this;
        }

        /**
         * Puts the key/value pair in the map, redacting the value and only recording if the value
         * is null or not.
         *
         * <p>For non-sensitive and enumerable values use {@link #putListed(String, String)}
         *
         * @param key Key identifying the field.
         * @param value Sensitive or innumerable value of the field.
         */
        public Builder putRedacted(@Nonnull String key, @Nullable Date value) {
            put(key, toSecret(value));
            return this;
        }

        /**
         * Put explicit null for the given key in the map.
         *
         * @param key Key identifying the field.
         */
        public Builder putNull(@Nonnull String key) {
            put(key, "null");
            return this;
        }

        public TrackingList build() {
            return new TrackingList(fields);
        }

        private void put(String key, String value) {

            fields.add(FieldEntry.of(keyBase + key, String.valueOf(Strings.emptyToNull(value))));
        }

        private String toSecret(final String secret) {
            return Strings.isNullOrEmpty(secret) ? "null" : VALUE_NOT_LISTED;
        }

        private String toSecret(final Number secret) {
            return secret == null ? "null" : VALUE_NOT_LISTED;
        }

        private String toSecret(final Date secret) {
            return secret == null ? "null" : VALUE_NOT_LISTED;
        }
    }
}
