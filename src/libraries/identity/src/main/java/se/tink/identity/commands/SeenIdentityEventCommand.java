package se.tink.libraries.identity.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import se.tink.backend.core.Currency;

public class SeenIdentityEventCommand {
    private String userId;
    private List<String> identityIds;
    private Locale locale;
    private Currency currency;

    public SeenIdentityEventCommand(String userId, List<String> identityIds, Locale locale, Currency currency) {
        validate(userId, identityIds);
        this.userId = userId;
        this.identityIds = identityIds;
        this.locale = locale;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getIdentityIds() {
        return identityIds;
    }

    public Locale getLocale() {
        return locale;
    }

    public Currency getCurrency() {
        return currency;
    }

    private void validate(String userId, List<String> identityIds) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(Objects.nonNull(identityIds) && identityIds.size()>0);
    }
}
