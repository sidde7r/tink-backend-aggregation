package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.LocalizableKey;

@JsonObject
public class LoginErrorResponse {
    private int numberOfLoginAttemptsLeft;
    private int numberOfLoginAttempts;
    private boolean blocked;
    private boolean lastChance;
    private String errorMessage;
    private int errorCode;
    private List<Object> suppressed;
    private boolean missingAgreements;
    private int loginErrorNumber;
    private String status;
    private String debugMessage;

    public int getNumberOfLoginAttemptsLeft() {
        return numberOfLoginAttemptsLeft;
    }

    public int getNumberOfLoginAttempts() {
        return numberOfLoginAttempts;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isLastChance() {
        return lastChance;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<Object> getSuppressed() {
        return suppressed;
    }

    public boolean isMissingAgreements() {
        return missingAgreements;
    }

    public int getLoginErrorNumber() {
        return loginErrorNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    private LocalizableKey getUserMessage() {
        return new LocalizableKey(errorMessage);
    }

    public void throwException() throws LoginException, AuthorizationException {

        if (numberOfLoginAttemptsLeft == 1) {
            throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception(getUserMessage());
        }

        if (blocked) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(getUserMessage());
        }

        switch (errorCode) {
            case 109:
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(getUserMessage());
            case 112:
                throw LoginError.INCORRECT_CREDENTIALS.exception(getUserMessage());
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        new LocalizableKey(String.format("Unknown error code %d", errorCode)));
        }
    }
}
