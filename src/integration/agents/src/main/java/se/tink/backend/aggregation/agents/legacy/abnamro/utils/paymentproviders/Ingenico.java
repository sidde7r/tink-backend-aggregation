package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Ingenico extends PaymentProvider {

    private static final Pattern NAME = Pattern.compile("INGENICO", Pattern.CASE_INSENSITIVE);

    /**
     * Format is a lot of digits and spaces (at least 20) and then the title of the merchant.
     * Example: 3293946918 0020001 435300729 164814934 Van Uffelen Mode B.V.
     */
    private static final Pattern DIGIT_PATTERN =
            Pattern.compile("(\\d|\\s|-){20,}(?<value>\\D.{5,})", Pattern.CASE_INSENSITIVE);

    private static final ImmutableList<Pattern> patterns =
            ImmutableList.of(ORDER_PATTERN, DIGIT_PATTERN);

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
