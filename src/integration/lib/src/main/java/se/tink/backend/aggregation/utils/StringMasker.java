package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.function.Predicate;

public class StringMasker {

    public static final String MASK = "***MASKED***";
    private static final Predicate<String> NONE_WHITELISTED = (s -> false);

    private static final Comparator<String> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparing(String::length)
                    .reversed()
                    .thenComparing(Comparator.naturalOrder());

    private final ImmutableList<String> sensitiveValuesToMask;

    /**
     * Compose a StringMasker from the given builders which can the be used to censor strings.
     *
     * @param builders Builders used to compose this masker.
     */
    public StringMasker(Iterable<StringMaskerBuilder> builders) {
        this(builders, NONE_WHITELISTED);
    }

    /**
     * Compose a StringMasker from the given builders which can the be used to censor strings.
     *
     * @param builders Builders used to compose this masker.
     * @param isWhiteListedPredicate Predicate that can override strings to be masked by returning
     *     true for these.
     */
    public StringMasker(
            Iterable<StringMaskerBuilder> builders, Predicate<String> isWhiteListedPredicate) {

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        builders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        ImmutableSet<String> sensitiveValuesWithoutDuplicates = builder.build();

        sensitiveValuesToMask =
                sensitiveValuesWithoutDuplicates.stream()
                        .filter(isWhiteListedPredicate.negate())
                        .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR)
                        .collect(ImmutableList.toImmutableList());
    }

    /**
     * @param string The string to be masked.
     * @return A new String with any values this masker deems sensitive masked.
     */
    public String getMasked(final String string) {
        return sensitiveValuesToMask.stream()
                .reduce(string, (s1, value) -> s1.replaceAll(value, MASK));
    }
}
