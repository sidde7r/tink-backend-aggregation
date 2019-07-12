package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import java.util.List;
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
}
