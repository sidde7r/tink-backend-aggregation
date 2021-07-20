package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TriTypeMapper<Enum, Coll, Mapper extends TriTypeMapper> {

    private static final Logger logger = LoggerFactory.getLogger(TriTypeMapper.class);

    private final ImmutableMap<String, Tuple2<Enum, List<Coll>>> translator;
    private final ImmutableSet<String> ignoredKeys;

    private BiPredicate<String, Collection<Enum>> isOneOfType =
            (input, types) -> translate(input.toLowerCase()).map(types::contains).orElse(false);

    TriTypeMapper(Builder<Enum, Coll, Mapper> builder) {
        this.ignoredKeys =
                ImmutableSet.copyOf(
                        Optional.ofNullable(builder.ignoredKeys).orElse(new HashSet<>()));
        this.translator =
                builder.translator.entrySet().stream()
                        .peek(
                                entry ->
                                        Preconditions.checkState(
                                                !ignoredKeys.contains(entry.getKey()),
                                                String.format(
                                                        "Key %s is both mapped and ignored",
                                                        entry.getKey())))
                        .collect(
                                Collectors.collectingAndThen(
                                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue),
                                        ImmutableMap::copyOf));
    }

    /**
     * Returns the type associated with the key, if any. A warning is logged if the key cannot be
     * mapped to a type, unless the key has been ignored.
     */
    public Optional<Enum> translate(String typeKey) {
        if (typeKey == null) {
            logger.warn("Received translation request for null type key");
            return Optional.empty();
        }

        final String key = typeKey.toLowerCase();

        return getTypeFromKey(key);
    }

    /**
     * Returns the type associated with the pattern, if any. A warning is logged if the key cannot
     * be mapped to a type, unless the key has been ignored.
     */
    public Optional<Enum> translateByPattern(String typeKey) {
        if (typeKey == null) {
            logger.warn("Received translation request for null type key");
            return Optional.empty();
        }

        final String key =
                translator.keySet().stream()
                        .filter(s -> typeKey.toLowerCase().contains(s))
                        .findFirst()
                        .orElse(typeKey.toLowerCase());

        return getTypeFromKey(key);
    }

    private Optional<Enum> getTypeFromKey(String key) {
        Optional<Enum> type = Optional.ofNullable(translator.get(key)).map(Tuple2::_1);

        if (!type.isPresent() && !ignoredKeys.contains(key)) {
            logger.warn("Unknown account type for key: {}", key);
            return Optional.empty();
        }

        return type;
    }

    public List<Coll> getItems(String typeKey) {
        return typeKey == null
                ? Collections.emptyList()
                : Optional.ofNullable(translator.get(typeKey.toLowerCase()))
                        .map(Tuple2::_2)
                        .orElse(Collections.emptyList());
    }

    public boolean isOneOf(String input, Collection<Enum> types) {
        return isOneOfType.test(input, types);
    }

    public boolean isOf(String input, Enum type) {
        return isOneOfType.test(input, Collections.singleton(type));
    }

    public abstract static class Builder<Enum, Coll, Mapper extends TriTypeMapper> {

        private final Set<String> ignoredKeys = new HashSet<>();
        private final Map<String, Tuple2<Enum, List<Coll>>> translator = new HashMap<>();
        private final Set<String> knownKeys = new HashSet<>();

        public abstract Builder<Enum, Coll, Mapper> buildStep();

        public Builder<Enum, Coll, Mapper> put(Enum value, Coll flag, String... keys) {
            put(value, Collections.singletonList(flag), keys);
            return buildStep();
        }

        public Builder<Enum, Coll, Mapper> put(Enum value, List<Coll> flags, String... keys) {
            Arrays.stream(keys)
                    .map(String::toLowerCase)
                    .filter(knownKeys::contains)
                    .findFirst()
                    .ifPresent(
                            key -> {
                                throw new IllegalArgumentException(
                                        String.format("Key %s already mapped", key));
                            });

            Arrays.stream(keys)
                    .map(String::toLowerCase)
                    .peek(knownKeys::add)
                    .forEach(key -> translator.put(key, Tuple.of(value, flags)));
            return buildStep();
        }

        /**
         * Known keys, and the account type they should be mapped to. Mapping is case insensitive.
         */
        public Builder<Enum, Coll, Mapper> put(Enum value, String... keys) {
            put(value, Lists.newArrayList(), keys);
            return buildStep();
        }

        /**
         * Known keys that should not be mapped to any specific account type. This means that a
         * warning will not be printed when attempting to map these keys.
         */
        public Builder<Enum, Coll, Mapper> ignoreKeys(String... keys) {
            ignoredKeys.addAll(
                    Arrays.stream(keys).map(String::toLowerCase).collect(Collectors.toList()));
            return this;
        }

        public abstract Mapper build();
    }
}
