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

    private static final Locale LOCALE_DA = new Locale("da");
    private static final String DEFAULT_SEPARATOR = " ";

    static ExactCurrencyAmount parse(final String s) {
        String[] parts = s.split(DEFAULT_SEPARATOR);
        if (parts.length != 2) {
            return null;
        }

        try {
            return ExactCurrencyAmount.of(
                    NumberFormat.getNumberInstance(LOCALE_DA).parse(parts[0]).doubleValue(),
                    parts[1]);
        } catch (ParseException e) {
            log.error("Could not parse value {} to locale {}", parts[0], LOCALE_DA);
            return null;
        }
    }
}
