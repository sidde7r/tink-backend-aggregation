package se.tink.backend.rpc.loans;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import se.tink.libraries.validation.validators.LocaleValidator;

public class GetLoanEventsCommand {
    private String userId;
    private String locale;

    public GetLoanEventsCommand(String userId, String locale) throws InvalidLocaleException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty");
        LocaleValidator.validate(locale);

        this.userId = userId;
        this.locale = locale;
    }

    public String getUserId() {
        return userId;
    }

    public String getLocale() {
        return locale;
    }
}
