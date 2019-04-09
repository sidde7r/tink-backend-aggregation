package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class GlobalCollect extends PaymentProvider {

    private static final Pattern NAMES = Pattern.compile("GlobalCollect", Pattern.CASE_INSENSITIVE);

    /** Special format for KLM transactions since they have a lot of them */
    private static final Pattern KLM_PATTERN =
            Pattern.compile(
                    "\\d{12}\\s\\d{12}.*?(KLM\\*Ref)\\s*.{6}(?<value>\\D{4,})$",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Format is `12 digits space 12 digits "anything" merchant name. Example: 111112945111
     * 1110001159792111 1211121212122 Steam
     */
    private static final Pattern DIGIT_PATTERN =
            Pattern.compile("\\d{12}\\s\\d{12}.*?(?<value>\\D{4,})$", Pattern.CASE_INSENSITIVE);

    private static final ImmutableList<Pattern> patterns =
            ImmutableList.of(KLM_PATTERN, DIGIT_PATTERN);

    @Override
    protected Pattern getNamePattern() {
        return NAMES;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
