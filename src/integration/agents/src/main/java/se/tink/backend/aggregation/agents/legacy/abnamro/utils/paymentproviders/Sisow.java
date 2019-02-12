package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Sisow extends PaymentProvider {

    private final static Pattern NAME = Pattern.compile("Stichting Sisow", Pattern.CASE_INSENSITIVE);

    private final static ImmutableList<Pattern> patterns = ImmutableList.of(ORDER_PATTERN);

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
