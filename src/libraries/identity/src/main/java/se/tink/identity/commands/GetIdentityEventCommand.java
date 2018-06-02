package se.tink.libraries.identity.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Objects;
import se.tink.backend.core.Currency;

public class GetIdentityEventCommand {
    private String id;
    private String userId;
    private Locale locale;
    private Currency currency;


    public GetIdentityEventCommand(String id, String userId, Locale locale, Currency currency) {
        validate(id, userId, locale, currency);
        this.id = id;
        this.userId = userId;
        this.locale = locale;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Locale getLocale() {
        return locale;
    }

    public Currency getCurrency() {
        return currency;
    }

    private void validate(String id, String userId, Locale locale, Currency currency) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(Objects.nonNull(locale));
        Preconditions.checkArgument(Objects.nonNull(currency));
    }
}
