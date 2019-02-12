package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class WorldPay extends PaymentProvider {

    private final static Pattern NAME = Pattern.compile("WORLDPAY AP LTD", Pattern.CASE_INSENSITIVE);

    /**
     * Common used pattern with 000 and then three digits and then merchant name
     * - "whatever whatever whatever whatever 000123Betsson"
     */
    private final static Pattern pattern1 = Pattern
            .compile("\\s000\\d{3}(?<value>(\\D){3,})$", Pattern.CASE_INSENSITIVE);

    private final static ImmutableList<Pattern> patterns = ImmutableList.of(pattern1);

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
