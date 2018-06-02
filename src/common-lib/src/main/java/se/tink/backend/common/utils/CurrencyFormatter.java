package se.tink.backend.common.utils;

import java.util.Locale;
import se.tink.backend.core.Currency;

public class CurrencyFormatter {
    private final Currency currency;
    private final Locale locale;

    public CurrencyFormatter(Currency currency, Locale locale) {
        this.currency = currency;
        this.locale = locale;
    }

    public String formatCurrencyRound(double amount) {
        return I18NUtils.formatCurrencyRound(amount, currency, locale);
    }
}
