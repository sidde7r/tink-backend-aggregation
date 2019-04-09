package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class MultisafePay extends PaymentProvider {

    private static final Pattern NAMES = Pattern.compile("MultiSafepay", Pattern.CASE_INSENSITIVE);

    private static final ImmutableList<Pattern> PATTERNS = ImmutableList.of(ORDER_PATTERN);

    @Override
    protected Pattern getNamePattern() {
        return NAMES;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return PATTERNS;
    }
}
