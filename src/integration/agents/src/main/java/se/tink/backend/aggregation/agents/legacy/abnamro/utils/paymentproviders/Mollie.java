package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Mollie extends PaymentProvider {

    private static final Pattern NAME =
            Pattern.compile("Stg Mollie Payments", Pattern.CASE_INSENSITIVE);

    /** Hard to match anything on this provider right now */
    private static final ImmutableList<Pattern> patterns = ImmutableList.of();

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
