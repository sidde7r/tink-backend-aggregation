package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CurrencyMapper {

    private static Map<Integer, Currency> currencies = new HashMap<>();

    static {
        Currency.getAvailableCurrencies()
                .forEach(currency -> currencies.put(currency.getNumericCode(), currency));
    }

    Currency get(Integer iso4217Code) {
        return Optional.ofNullable(currencies.get(iso4217Code))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Currency with numeric code "
                                                + iso4217Code
                                                + " not found"));
    }
}
