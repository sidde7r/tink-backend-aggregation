package se.tink.libraries.identity.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Objects;
import se.tink.backend.core.Currency;

public class GetIdentityEventSummaryListCommand {
    private String userId;
    private Locale locale;
    private Currency currency;

    public GetIdentityEventSummaryListCommand(String userId, Locale locale, Currency currency) {
        validate(userId, locale, currency);
        this.userId = userId;
        this.locale = locale;
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getUserId() {
        return userId;
    }

    public Locale getLocale() {
        return locale;
    }

    private void validate(String userId, Locale locale, Currency currency) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(Objects.nonNull(locale));
        Preconditions.checkArgument(Objects.nonNull(currency));
    }

}
