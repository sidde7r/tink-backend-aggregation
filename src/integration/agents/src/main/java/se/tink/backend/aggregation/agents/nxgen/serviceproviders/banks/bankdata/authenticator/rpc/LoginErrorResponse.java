package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.annotations.JsonObject;

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

    public void throwException() throws LoginException, AuthorizationException {

        if (numberOfLoginAttemptsLeft == 1) {
            throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception(errorMessage);
        }

        if (blocked) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(errorMessage);
        }

        switch (errorCode) {
            case 109:
                handle109ErrorCode();
                break;
            case 112:
                throw LoginError.INCORRECT_CREDENTIALS.exception(errorMessage);
            default:
                throw LoginError.DEFAULT_MESSAGE.exception("Unknown error code " + errorCode);
        }
    }

    private void handle109ErrorCode() {
        switch (loginErrorNumber) {
            case 1:
            case 7:
                throw LoginError.INCORRECT_CREDENTIALS.exception(errorMessage);
            case 2:
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(errorMessage);
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        String.format(
                                "Unknown login error number %d for error code %d",
                                loginErrorNumber, errorCode));
        }
    }
}
