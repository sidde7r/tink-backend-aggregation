package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.strings.StringUtils;

public class AmountParser {

    private static final Pattern AMOUNT_FINDER = Pattern.compile(".*?(?<amount>\\d+[,.]\\d+).*");

    private final String text;

    public AmountParser(String text) {
        this.text = text;
    }

    public Optional<Double> parse() {
        return Optional.ofNullable(text)
                .map(AMOUNT_FINDER::matcher)
                .filter(Matcher::matches)
                .map(matcher -> StringUtils.parseAmount(matcher.group("amount")));
    }
}
