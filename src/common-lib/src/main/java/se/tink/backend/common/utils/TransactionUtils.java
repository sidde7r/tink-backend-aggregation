package se.tink.backend.common.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.core.Transaction;

public class TransactionUtils {
    // "Swish ... Förnamn Mellannamn Efternamn"
    // "Swish ... FÖRNAMN MELLANNAMN EFTERNAMN"
    private static final Pattern COMMON_SWISH_PATTERN_NAME = Pattern
            .compile("^Swish( \\pL+)? (?<name>(\\p{Lu}[\\pL-]+ ?)+)$");

    // "Swish ... Efternamn, Förnamn Mellannamn"
    // "Swish ... EFTERNAMN, FÖRNAMN MELLANNAMN"
    private static final Pattern COMMON_SWISH_PATTERN_NAME_REVERSE_ORDER = Pattern
            .compile("^Swish( \\pL+)? (?<lastname>\\p{Lu}[\\pL-]+), (?<firstname>(\\p{Lu}[\\pL-]+ ?)+)$");

    // "Swish ... +46NNNNNNNN"
    private static final Pattern COMMON_SWISH_PATTERN_PHONE_NUMBER = Pattern
            .compile("^Swish( \\w+)? (?<phonenumber>\\+46[1-9]\\d{7,8})$");

    // "46NNNNNNNN"
    private static final Pattern SEB_SWISH_PATTERN = Pattern.compile("^(?<phonenumber>46[1-9]\\d{7,8})$");

    private static final List<Pattern> SWISH_PATTERNS = ImmutableList
            .of(COMMON_SWISH_PATTERN_NAME, COMMON_SWISH_PATTERN_NAME_REVERSE_ORDER, COMMON_SWISH_PATTERN_PHONE_NUMBER,
                    SEB_SWISH_PATTERN);

    public static boolean isSwish(Transaction transaction) {
        return isSwish(transaction.getOriginalDescription());
    }

    public static boolean isSwish(String description) {
        if (Strings.isNullOrEmpty(description)) {
            return false;
        }

        for (Pattern pattern : SWISH_PATTERNS) {
            if (pattern.matcher(description).find()) {
                return true;
            }
        }

        return false;
    }

    public static Optional<String> getNameFromSwishTransaction(Transaction transaction) {
        String description = transaction.getOriginalDescription();

        if (Strings.isNullOrEmpty(description)) {
            return Optional.empty();
        }

        Matcher matcher;

        matcher = COMMON_SWISH_PATTERN_NAME.matcher(description);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("name"));
        }

        matcher = COMMON_SWISH_PATTERN_NAME_REVERSE_ORDER.matcher(description);
        if (matcher.find()) {
            return Optional.of(String.format("%s %s", matcher.group("firstname"), matcher.group("lastname")));
        }

        return Optional.empty();
    }

    public static Optional<String> getPhoneNumberFromSwishTransaction(Transaction transaction) {
        String description = transaction.getOriginalDescription();

        if (Strings.isNullOrEmpty(description)) {
            return Optional.empty();
        }

        Matcher matcher;

        matcher = COMMON_SWISH_PATTERN_PHONE_NUMBER.matcher(description);
        if (matcher.find()) {
            String phoneNumber = matcher.group("phonenumber");
            return Optional.of(phoneNumber.replaceFirst("^\\+46", "0"));
        }

        matcher = SEB_SWISH_PATTERN.matcher(description);
        if (matcher.find()) {
            String phoneNumber = matcher.group("phonenumber");
            return Optional.of(phoneNumber.replaceFirst("^46", "0"));
        }

        return Optional.empty();
    }
}
