package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract payment provider class that is used for description matching
 */
public abstract class PaymentProvider {

    protected abstract Pattern getNamePattern();

    protected abstract ImmutableList<Pattern> getDescriptionPatterns();

    /**
     * Stores list of whitespace positions that needs to be cleaned.
     */
    private final static ImmutableSortedSet<Integer> WHITE_SPACE_POSITIONS = ImmutableSortedSet.of(84, 51, 50, 18)
            .descendingSet();

    /**
     * Matches web addresses either on for example the form "www.pay.nl", "pay.com", or "http://www.erik.se"
     *
     * Note, this is not a "correct pattern" in the sense that it will match all valid domain names. Some cases are
     * ignored by purpose. See test cases for examples.
     *
     * Some patterns that we don't match:
     * 123abc.com => A valid domain name but we don't allow it to start with digits
     * www.aaa.bbb.com => A valid sub domain but we don't allow
     */
    private final static Pattern DOMAIN_PATTERN = Pattern
            .compile("(^|\\s)(http(s)?(:)?\\/\\/)?(?<value>(www\\.)?[A-z]([^\\s\\.]|-){2,}\\.(nl|com|eu|se))",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Matches order patterns, used in payment providers that needs this
     */
    protected final static Pattern ORDER_PATTERN = Pattern.compile(
            "(Order|Order\\(s\\)|Bestelling|Bestelnummer):?\\s((([\\D]*)\\d+)\\s)?((at\\s?)|(@\\s?))?(:\\s)?(?<value>\\w.{3,})$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Check that the name matches against the name of the payment provider
     */
    public boolean matches(String name) {
        return !Strings.isNullOrEmpty(name) && getNamePattern().matcher(name).matches();
    }

    /**
     * Get a new description from the original one. Null if we couldn't extract one.
     */
    public String getDescription(String description) {

        description = removeWhitespaces(description);

        String domainNameMatch = matchDomainNamePattern(description);

        if (domainNameMatch != null) {
            return domainNameMatch;
        }

        return matchProviderSpecificPatterns(description);
    }

    /**
     * Match description against domain name patterns
     */
    private static String matchDomainNamePattern(String description) {

        Matcher domainMatcher = DOMAIN_PATTERN.matcher(description);

        String longestMatch = null;

        // Return the longest name if we have multiple matches. This is done since we have cases where we
        // have both "google.com" and "goog le.com" and then we want to pick the longest one.
        while (domainMatcher.find()) {
            String match = domainMatcher.group("value").trim();

            if (longestMatch == null || match.length() > longestMatch.length()) {
                longestMatch = match;
            }
        }

        return longestMatch;
    }

    /**
     * Match description against payment provider specific patterns
     */
    private String matchProviderSpecificPatterns(String description) {
        for (Pattern pattern : getDescriptionPatterns()) {
            Matcher matcher = pattern.matcher(description);

            if (matcher.find()) {

                String result = matcher.group("value");

                if (!Strings.isNullOrEmpty(result)) {
                    return result.trim();
                }
            }
        }

        return null;
    }

    /**
     * Remove any whitespaces if needed
     */
    private String removeWhitespaces(String description) {
        StringBuilder builder = new StringBuilder(description);

        for (Integer position : WHITE_SPACE_POSITIONS) {
            if (description.length() > position && description.charAt(position) == ' ') {
                builder.deleteCharAt(position);
            }
        }

        return builder.toString();
    }
}
