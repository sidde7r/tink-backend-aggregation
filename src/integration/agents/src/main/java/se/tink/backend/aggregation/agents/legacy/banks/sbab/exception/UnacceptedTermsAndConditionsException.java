package se.tink.backend.aggregation.agents.banks.sbab.exception;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class UnacceptedTermsAndConditionsException extends AuthorizationException {
    private static LocalizableKey USER_MESSAGE =
            new LocalizableKey(
                    "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website.");
    private String url;
    private MultivaluedMapImpl input;

    public UnacceptedTermsAndConditionsException(String url, MultivaluedMapImpl input) {
        super(AuthorizationError.ACCOUNT_BLOCKED, USER_MESSAGE);
        this.url = url;
        this.input = input;
    }

    public String getUrl() {
        return url;
    }

    public MultivaluedMapImpl getInput() {
        return input;
    }
}
