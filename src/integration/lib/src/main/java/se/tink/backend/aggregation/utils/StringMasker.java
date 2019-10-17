package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;

public class StringMasker {

    public static final String MASK = "***MASKED***";

    private static final Comparator<String> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparing(String::length)
                    .reversed()
                    .thenComparing(Comparator.naturalOrder());

    private final ImmutableList<String> sensitiveValuesToMask;

    /**
     * Compose a StringMasker from the given builders which can the be used to censor strings.
     * @param builders Builders used to compose this masker.
     */
    public StringMasker(Iterable<StringMaskerBuilder> builders) {

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        ImmutableSet<String> sensitiveValuesWithoutDuplicates = builder.build();

        sensitiveValuesToMask =
                sensitiveValuesWithoutDuplicates.stream()
                        .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR)
                        .collect(ImmutableList.toImmutableList());
    }

    /**
     *
     * @param string The string to be masked.
     * @return A new String with any values this masker deems sensitive masked.
     */
    public String getMasked(final String string) {
        return sensitiveValuesToMask.stream()
                .reduce(string, (s1, value) -> s1.replaceAll(value, MASK));
    }
}
