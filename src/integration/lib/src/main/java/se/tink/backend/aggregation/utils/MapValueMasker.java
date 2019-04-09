package se.tink.backend.aggregation.utils;

import java.util.Collection;
import java.util.Map;

/**
 * A masker that takes a map as input and masks all values (not keys) relevant according to the
 * implementation.
 */
public interface MapValueMasker {
    /**
     * @return A copy of the string map with all values masked that should be masked according to
     *     implementation
     */
    Map<String, String> copyAndMaskValues(Map<String, String> map);

    Map<String, Collection<String>> copyAndMaskMultiValues(
            Map<String, ? extends Collection<String>> multiValues);
}
