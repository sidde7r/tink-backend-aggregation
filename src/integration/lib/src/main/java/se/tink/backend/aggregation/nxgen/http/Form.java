package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Preconditions;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public final class Form {
    private LinkedHashMap<String, String> parameters = new LinkedHashMap<>();

    private static final String JOINING_DELIMITER = "&";
    private static final String NAME_VALUE_FORMAT = "%s=%s";

    public String serialize() {
        return parameters.entrySet().stream()
                .map(this::getValuePair)
                .collect(Collectors.joining(JOINING_DELIMITER));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return A new builder from this form, which can be re-built after adding additional fields
     */
    public Builder rebuilder() {
        Form.Builder formBuilder = Form.builder();
        formBuilder.parameters.putAll(this.parameters);
        return formBuilder;
    }

    private String getValuePair(Map.Entry<String, String> parameter) {
        try {
            final String key = URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8.toString());
            if (parameter.getValue() == null) {
                return key;
            }
            final String value = URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8.toString());
            return String.format(NAME_VALUE_FORMAT, key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create form body: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return serialize();
    }

    public static class Builder {

        private final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();

        private Builder() {}

        /**
         * Add key-value parameter.
         */
        public Builder put(@Nonnull String key, @Nonnull String value) {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(value);
            parameters.put(key, value);
            return this;
        }

        /**
         * Add parameter without a value.
         */
        public Builder put(@Nonnull String key) {
            Preconditions.checkNotNull(key);
            parameters.put(key, null);
            return this;
        }

        public Form build() {
            Form form = new Form();
            form.parameters = new LinkedHashMap<>(this.parameters);
            return form;
        }
    }
}
