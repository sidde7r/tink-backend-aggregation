package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
class ToExactCurrencyAmountMapper {

    private final Locale locale;
    private final String separator;

    public ExactCurrencyAmount parse(final String s) {
        String[] parts = s.split(separator);
        if (parts.length != 2) {
            return null;
        }

        try {
            return ExactCurrencyAmount.of(
                    NumberFormat.getNumberInstance(locale).parse(parts[0]).doubleValue(), parts[1]);
        } catch (ParseException e) {
            log.error("Could not parse value {} to locale {}", parts[0], locale);
            return null;
        }
    }
}
