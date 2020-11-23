package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agentsplatform.framework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.framework.error.SessionExpiredError;

@AllArgsConstructor
@Getter
public enum LoginResponseStatus {
    NO_ERRORS(null),
    INCORRECT_CREDENTIALS(new InvalidCredentialsError()),
    ACCOUNT_BLOCKED(new AccountBlockedError()),
    SESSION_EXPIRED(new SessionExpiredError());

    private AgentBankApiError error;

    public boolean isError() {
        return !this.equals(NO_ERRORS);
    }
}
