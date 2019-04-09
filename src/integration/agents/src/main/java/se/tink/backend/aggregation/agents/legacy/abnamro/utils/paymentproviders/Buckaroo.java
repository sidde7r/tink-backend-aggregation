package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Buckaroo extends PaymentProvider {

    private static final Pattern NAME =
            Pattern.compile("Stichting Derdengelden Buckaroo", Pattern.CASE_INSENSITIVE);

    /**
     * Think this isn't good enough to be added. Will evaluate it in production before adding it.
     * /EP
     */
    private static final Pattern PATTERN_1 =
            Pattern.compile("(\\s|\\d)(?<value>(\\D){3,})$", Pattern.CASE_INSENSITIVE);

    private static final ImmutableList<Pattern> PATTERNS = ImmutableList.of();

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return PATTERNS;
    }
}
