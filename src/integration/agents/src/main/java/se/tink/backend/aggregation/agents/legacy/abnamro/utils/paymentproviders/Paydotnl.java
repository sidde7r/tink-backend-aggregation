package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Paydotnl extends PaymentProvider {

    private final static Pattern NAMES = Pattern.compile("Stichting Pay.nl", Pattern.CASE_INSENSITIVE);

    /**
     * Transactions are on the format "whatever whatever (at least three digits or dash) merchant name" like
     * ... 71201 116 MamaLoes
     * ... 1229-010 Tinder
     */
    private final static Pattern PATTERN_1 = Pattern
            .compile("(\\d|-){3}\\s(?<value>(\\D){3,})$", Pattern.CASE_INSENSITIVE);

    private final static ImmutableList<Pattern> patterns = ImmutableList.of(PATTERN_1);

    @Override
    protected Pattern getNamePattern() {
        return NAMES;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
