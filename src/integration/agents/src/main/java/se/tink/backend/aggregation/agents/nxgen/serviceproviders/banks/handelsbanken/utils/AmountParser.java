package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.strings.StringUtils;

public class AmountParser {

    private static final Pattern AMOUNT_FINDER = Pattern.compile(".*?(?<amount>\\d+[,.]\\d+).*");

    private final String text;

    public AmountParser(String text) {
        // Replace all whitespaces, and explicitly non breaking space, as it was not covered by \s
        this.text = text.replaceAll("[\\s\\u00a0]+", "");
    }

    public Optional<Double> parse() {
        return Optional.ofNullable(text)
                .map(AMOUNT_FINDER::matcher)
                .filter(Matcher::matches)
                .map(matcher -> StringUtils.parseAmount(matcher.group("amount")));
    }
}
