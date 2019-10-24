package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringMasker {

    public static final String MASK = "***MASKED***";
    private static final Predicate<Pattern> NONE_WHITELISTED = (p -> false);

    private static final Comparator<Pattern> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparingInt(p -> p.toString().length());

    private final ImmutableList<Pattern> sensitiveValuesToMask;

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
            Iterable<StringMaskerBuilder> builders, Predicate<Pattern> isWhiteListedPredicate) {

        ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();

        builders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        ImmutableSet<Pattern> sensitiveValuesWithoutDuplicates = builder.build();

        sensitiveValuesToMask =
                sensitiveValuesWithoutDuplicates.stream()
                        .filter(isWhiteListedPredicate.negate())
                        .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR.reversed())
                        .collect(ImmutableList.toImmutableList());
    }

    /**
     * @param string The string to be masked.
     * @return A new String with any values this masker deems sensitive masked.
     */
    public String getMasked(final String string) {

        String result = string;
        for (Pattern p : sensitiveValuesToMask) {
            result = p.matcher(result).replaceAll(MASK);
        }

        return result;
    }
}
