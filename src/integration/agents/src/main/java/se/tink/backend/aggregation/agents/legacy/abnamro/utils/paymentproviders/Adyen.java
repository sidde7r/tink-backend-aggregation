package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class Adyen extends PaymentProvider {

    /**
     * Name is "STG ADYEN". They also have the name "Burger King By Adyen" but these are cleaned in
     * the description formatter
     */
    private static final Pattern NAME = Pattern.compile("STG ADYEN", Pattern.CASE_INSENSITIVE);

    /**
     * Common used pattern with the merchant in the beginning of the string and then only numbers or
     * whitespaces. Requiring at least 4 non digit characters to skip cases where we only have a
     * couple of characters - SpotifyNL 1911212121 - Unibet 32423 23222
     */
    private static final Pattern PATTERN_1 =
            Pattern.compile("^(?<value>\\D{4,})(\\d|\\s)*", Pattern.CASE_INSENSITIVE);

    /* Common used pattern is that it starts with a 16 digit long number and then numbers or characters
     * - 1114511873114104 1130111431202011 1100043266 00111 8HappySocks
     * - 1114511873114104 Some characters 00111 8HappySocks
     */
    private static final Pattern PATTERN_2 =
            Pattern.compile("\\d{16}.*?(?<value>\\D{4,})$", Pattern.CASE_INSENSITIVE);

    private static final ImmutableList<Pattern> patterns = ImmutableList.of(PATTERN_1, PATTERN_2);

    @Override
    protected Pattern getNamePattern() {
        return NAME;
    }

    @Override
    protected ImmutableList<Pattern> getDescriptionPatterns() {
        return patterns;
    }
}
