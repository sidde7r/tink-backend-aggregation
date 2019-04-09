package se.tink.backend.aggregation.utils;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class MapValueMaskerImpl implements MapValueMasker {
    private static final String MASK = "***MASKED***";

    private final Maps.EntryTransformer<String, String, String> mapTransformer;
    private final Maps.EntryTransformer<String, Collection<String>, Collection<String>>
            multiValueMapTransformer;

    /**
     * @param whiteListedKeys Set of keys of which values will NOT be masked, if absent all values
     *     are masked
     */
    public MapValueMaskerImpl(Optional<Set<String>> whiteListedKeys) {
        this.mapTransformer = createEntryTransformer(whiteListedKeys);
        this.multiValueMapTransformer = createMultivaluedEntryTransformer(whiteListedKeys);
    }

    private Maps.EntryTransformer<String, String, String> createEntryTransformer(
            final Optional<Set<String>> whiteListedKeys) {
        return (key, value) -> {
            if (key == null) {
                return null;
            }

            if (whiteListedKeys.isPresent()) {
                if (whiteListedKeys.get().stream()
                        .anyMatch(Predicates.containsCaseInsensitive(key))) {
                    return value;
                } else {
                    return MASK;
                }
            } else {
                return MASK;
            }
        };
    }

    private Maps.EntryTransformer<String, Collection<String>, Collection<String>>
            createMultivaluedEntryTransformer(final Optional<Set<String>> whiteListedKeys) {
        return new Maps.EntryTransformer<String, Collection<String>, Collection<String>>() {
            @Override
            public Collection<String> transformEntry(
                    @Nullable String key, @Nullable Collection<String> values) {
                if (key == null || values == null) {
                    return null;
                }

                if (whiteListedKeys.isPresent()) {
                    if (whiteListedKeys.get().stream()
                            .anyMatch(Predicates.containsCaseInsensitive(key))) {
                        return values;
                    } else {
                        return getMaskedCollectionOfSameSize(values.size());
                    }
                } else {
                    return getMaskedCollectionOfSameSize(values.size());
                }
            }

            private Collection<String> getMaskedCollectionOfSameSize(int size) {
                String[] values = new String[size];
                Arrays.fill(values, MASK);
                return Arrays.asList(values);
            }
        };
    }

    @Override
    public Map<String, String> copyAndMaskValues(Map<String, String> map) {
        return Maps.transformEntries(map, mapTransformer);
    }

    @Override
    public Map<String, Collection<String>> copyAndMaskMultiValues(
            Map<String, ? extends Collection<String>> map) {
        return Maps.transformEntries(map, multiValueMapTransformer);
    }
}
