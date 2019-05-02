package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericTypeMapper<V, T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericTypeMapper.class);
    protected final Map<T, V> translator;
    protected final Set<T> ignoredKeys;
    private BiPredicate<T, Collection<V>> isOneOfType =
            (input, types) -> {
                Optional<V> type = translate(input);
                return type.map(types::contains).orElseGet(() -> false);
            };

    protected GenericTypeMapper(GenericTypeMapper.Builder<V, T, ?> builder) {
        ignoredKeys = builder.getIgnoredKeys();

        ImmutableMap.Builder<T, V> tmpTranslator = ImmutableMap.builder();
        for (Map.Entry<V, Collection<T>> entry : builder.getReversed().entrySet()) {

            entry.getValue()
                    .stream()
                    .peek(
                            key ->
                                    Preconditions.checkState(
                                            !ignoredKeys.contains(key),
                                            String.format(
                                                    "Key %s is both mapped and ignored.", key)))
                    .forEach(key -> tmpTranslator.put(key, entry.getKey()));
        }
        translator = tmpTranslator.build();
    }

    protected GenericTypeMapper() {
        translator = new HashMap<>();
        ignoredKeys = new HashSet<>();
    }

    public static <V, T> Builder<V, T, ?> genericBuilder() {
        return new GenericBuilder<>();
    }

    protected boolean verify(T key, V value) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    protected boolean verify(T key, Collection<V> values) {
        Optional<V> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    /**
     * Returns the type associated with the key, if any. A warning is logged if the key cannot be
     * mapped to a type, unless the key has been ignored.
     */
    public Optional<V> translate(T typeKey) {
        if (Objects.isNull(typeKey)) {
            return Optional.empty();
        }

        Optional<V> type = Optional.ofNullable(translator.get(typeKey));

        if (!type.isPresent() && !ignoredKeys.contains(typeKey)) {
            logger.warn("Unknown account type for key: {}", typeKey);
        }

        return type;
    }

    public boolean isOneOf(T input, Collection<V> types) {
        return isOneOfType.test(input, types);
    }

    public boolean isOf(T input, V type) {
        return isOneOfType.test(input, Collections.singleton(type));
    }

    public abstract static class Builder<V, T, B extends Builder<V, T, B>> {

        protected Builder() {
            this.thisObj = self();
        }

        protected final Map<V, Collection<T>> reversed = new HashMap<>();
        protected final Set<T> ignoredKeys = new HashSet<>();
        private B thisObj;

        public abstract GenericTypeMapper<V, T> build();

        /** Known keys, and the account type they should be mapped to. */
        public Builder<V, T, B> put(V value, T... keys) {
            self().reversed.put(value, Arrays.asList(keys));
            return self();
        }

        public Builder<V, T, B> putAll(Map<V, List<T>> map) {
            self().reversed.putAll(map);
            return self();
        }

        protected abstract B self();

        /** Known keys that should not be mapped to any specific account type. */
        /**
         * Known keys that should not be mapped to any specific account type. The effect is that a
         * warning will not be printed when attempting to map these keys.
         */
        public Builder<V, T, B> ignoreKeys(T... keys) {
            self().ignoredKeys.addAll(Arrays.asList(keys));
            return this;
        }

        protected Map<V, Collection<T>> getReversed() {
            return this.reversed;
        }

        protected Set<T> getIgnoredKeys() {
            return this.ignoredKeys;
        }
    }

    public static class GenericBuilder<V, T> extends Builder<V, T, GenericBuilder<V, T>> {
        public GenericBuilder() {
            super();
        }

        @Override
        protected GenericBuilder self() {
            return this;
        }

        public GenericTypeMapper<V, T> build() {
            return new GenericTypeMapper<>(this);
        }
    }
}
