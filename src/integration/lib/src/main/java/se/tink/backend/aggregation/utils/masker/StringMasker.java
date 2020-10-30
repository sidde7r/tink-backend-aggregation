package se.tink.backend.aggregation.utils.masker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class StringMasker {

    private static final String MASK = "**HASHED:%s**";
    private static final Predicate<Pattern> NONE_WHITELISTED = (p -> true);

    private static final Comparator<Pattern> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparingInt(p -> p.toString().length());

    private ImmutableList<Pattern> sensitiveValuesToMask;

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
     * @param shouldMaskPredicate Predicate that can override strings to be masked by returning true
     *     for these.
     */
    public StringMasker(
            Iterable<StringMaskerBuilder> builders, Predicate<Pattern> shouldMaskPredicate) {

        ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();

        builders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        ImmutableSet<Pattern> sensitiveValuesWithoutDuplicates = builder.build();

        sensitiveValuesToMask =
                sensitiveValuesWithoutDuplicates.stream()
                        .filter(shouldMaskPredicate)
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
            String replacement = String.format(MASK, hash(p));
            result = p.matcher(result).replaceAll(replacement);
        }

        return result;
    }

    private String hash(Pattern p) {
        String hash = Hash.sha256Base64(p.pattern().getBytes());
        int noMoreThat4 = Math.min(hash.length(), 4);
        return hash.substring(0, noMoreThat4);
    }

    public void addValuesToMask(
            StringMaskerBuilder stringMaskerBuilder, Predicate<Pattern> shouldMaskPredicate) {
        ImmutableSet<Pattern> newValuesToBeMasked =
                stringMaskerBuilder.getValuesToMask().stream()
                        .filter(shouldMaskPredicate)
                        .collect(ImmutableSet.toImmutableSet());

        ImmutableSet<Pattern> sensitiveValuesWithoutDuplicates =
                ImmutableSet.<Pattern>builder()
                        .addAll(newValuesToBeMasked)
                        .addAll(sensitiveValuesToMask)
                        .build();

        sensitiveValuesToMask =
                sensitiveValuesWithoutDuplicates.stream()
                        .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR.reversed())
                        .collect(ImmutableList.toImmutableList());
    }

    public void removeValuesToMask(Set<String> whitelistedValues) {
        sensitiveValuesToMask =
                (new HashSet<>(sensitiveValuesToMask))
                        .stream()
                                .filter(pattern -> !whitelistedValues.contains(pattern.toString()))
                                .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR.reversed())
                                .collect(ImmutableList.toImmutableList());
    }
}
