package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
                .map(TypeMapper::toKeyString)
                .collect(Collectors.toSet());

        ImmutableMap.Builder<String, V> tmpTranslator = ImmutableMap.builder();
        for (Map.Entry<V, Object[]> entry : builder.getReversed().entrySet()) {
            for (Object key : entry.getValue()) {
                String stringKey = toKeyString(key);

                if (ignoredKeys.contains(stringKey)) {
                    throw new IllegalArgumentException(String.format("Key %s is both mapped and ignored.", stringKey));
                }

                tmpTranslator.put(stringKey, entry.getKey());
            }
        }
        translator = tmpTranslator.build();
    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    private static String toKeyString(Object accountTypeKey) {
        return String.valueOf(accountTypeKey).toLowerCase();
    }

    protected boolean verify(Object key, V value) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    protected boolean verify(Object key, Collection<V> values) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    public Optional<V> translate(Object typeKey) {

        if (ignoredKeys.contains(toKeyString(typeKey))) {
            return Optional.empty();
        }

        V type = translator.get(toKeyString(typeKey));

        if (type == null) {
            logger.warn("Unknown account type for key: {}", typeKey);
            return Optional.empty();
        } else {
            return Optional.of(type);
        }
    }

    public static class Builder<V> {

        private final Map<V, Object[]> reversed = new HashMap<>();
        private final Set<Object> ignoredKeys = new HashSet<>();

        public TypeMapper<V> build() {
            return new TypeMapper<>(this);
        }

        /**
         * Known keys, and the account type they should be mapped to.
         */
        public Builder<V> put(V value, Object... keys) {
            reversed.put(value, keys);
            return this;
        }

        /**
         * Known keys that should not be mapped to any specific account type.
         */
        public Builder<V> ignoreKeys(Object... keys) {
            ignoredKeys.addAll(Arrays.asList(keys));
            return this;
        }

        private Map<V, Object[]> getReversed() {
            return reversed;
        }

        private Set<Object> getIgnoredKeys() {
            return ignoredKeys;
        }
    }
}
