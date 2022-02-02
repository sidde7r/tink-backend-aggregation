package se.tink.libraries.masker;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import se.tink.libraries.cryptography.hash.Hash;

/*
 * This class is used in masking log files stored in s3.
 * Instead of removing sensitive data it is replaced with SHA-256 hash converted to Base64 and shortened to 2 signs:
 *   **HASHED:<<2 character long base64 encoded hash>>**
 *
 * The purpose of hashing secrets is to see if some values change during refreshes process or between them.
 * This class does not take part in masking headers, only body.
 * */
public class StringMasker {

    private static final String STAR = "*";
    private static final int SHOWN_CHARS = 4;

    private static final String MASK = "**HASHED:%s**";

    private static final Comparator<Pattern> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparingInt(p -> p.toString().length());

    private ImmutableList<Pattern> sensitiveValuesToMask = ImmutableList.of();

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
        return hash.substring(0, 2);
    }

    public void addValuesToMask(
            MaskerPatternsProvider maskerPatternsProvider, Predicate<Pattern> shouldMaskPredicate) {
        ImmutableSet<Pattern> newValuesToBeMasked =
                maskerPatternsProvider.getPatternsToMask().stream()
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

    /**
     * Masks string to avoid showing sensitive data. For example, "ABC12345678909876543" would
     * become "ABC1~***~6543".
     */
    public static String maskMiddleOfString(String unmasked) {
        final String MASK = "~***~";

        if (Strings.isNullOrEmpty(unmasked)) {
            return unmasked;
        }
        if (unmasked.length() <= SHOWN_CHARS * 4) {
            return MASK;
        }

        int lastStarIndex = unmasked.length() - SHOWN_CHARS;
        return unmasked.substring(0, SHOWN_CHARS) + MASK + unmasked.substring(lastStarIndex);
    }

    /**
     * Masks string to avoid showing sensitive data. For example, "ABC12345678909876543" would
     * become "***********************6543".
     */
    public static String starMaskBeginningOfString(String unmasked) {
        if (Strings.isNullOrEmpty(unmasked)) {
            return unmasked;
        }
        if (unmasked.length() <= SHOWN_CHARS * 2) {
            return generateReplacement(unmasked.length());
        }

        int lastStar = unmasked.length() - SHOWN_CHARS;
        return generateReplacement(lastStar) + unmasked.substring(lastStar);
    }

    private static String generateReplacement(int length) {
        return new String(new char[length]).replace("\0", STAR);
    }
}
