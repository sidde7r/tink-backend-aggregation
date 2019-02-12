package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class ClickAndBuy extends PaymentProvider {

    private final static Pattern NAMES = Pattern.compile("ClickandBuy International.*", Pattern.CASE_INSENSITIVE);

    /**
     * Start with "ClickandBuy - " and the name of merchant, like "ClickandBuy - Tinder"
     */
    private static final Pattern PATTERN_1 = Pattern.compile("ClickandBuy\\s-\\s(?<value>.*)", Pattern.CASE_INSENSITIVE);

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
