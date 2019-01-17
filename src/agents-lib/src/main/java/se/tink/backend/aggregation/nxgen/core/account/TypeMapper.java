package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger(TypeMapper.class);
    private final Map<String, V> translator;
    private final Set<String> ignoredKeys;

    private TypeMapper(TypeMapper.Builder<V> builder) {
        super();

        ignoredKeys = builder.getIgnoredKeys()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        ImmutableMap.Builder<String, V> tmpTranslator = ImmutableMap.builder();
        for (Map.Entry<V, List<String>> entry : builder.getReversed().entrySet()) {

            entry.getValue()
                    .stream()
                    .map(String::toLowerCase)
                    .peek(key -> Preconditions.checkState(!ignoredKeys.contains(key),
                            String.format("Key %s is both mapped and ignored.", key)))
                    .forEach(key -> tmpTranslator.put(key, entry.getKey()));
        }
        translator = tmpTranslator.build();
    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    protected boolean verify(String key, V value) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    protected boolean verify(String key, Collection<V> values) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    public Optional<V> translate(String typeKey) {

        typeKey = typeKey.toLowerCase();
        Optional<V> type = Optional.ofNullable(translator.get(typeKey));

        if (!type.isPresent() && ignoredKeys.contains(typeKey)) {
            logger.warn("Unknown account type for key: {}", typeKey);
        }

        return type;
    }

    public static class Builder<V> {

        private final Map<V, List<String>> reversed = new HashMap<>();
        private final Set<String> ignoredKeys = new HashSet<>();

        public TypeMapper<V> build() {
            return new TypeMapper<>(this);
        }

        /**
         * Known keys, and the account type they should be mapped to.
         */
        public Builder<V> put(V value, String... keys) {

            reversed.put(value, Arrays.asList(keys));
            return this;
        }

        /**
         * Known keys that should not be mapped to any specific account type.
         */
        public Builder<V> ignoreKeys(String... keys) {
            ignoredKeys.addAll(Arrays.asList(keys));
            return this;
        }

        private Map<V, List<String>> getReversed() {
            return reversed;
        }

        private Set<String> getIgnoredKeys() {
            return ignoredKeys;
        }
    }
}
