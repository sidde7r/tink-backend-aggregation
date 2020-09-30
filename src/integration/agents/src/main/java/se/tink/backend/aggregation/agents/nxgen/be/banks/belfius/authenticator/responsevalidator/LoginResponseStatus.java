package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agentsplatform.framework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidCredentialsError;

@AllArgsConstructor
@Getter
public enum LoginResponseStatus {
    NO_ERRORS(null),
    INCORRECT_CREDENTIALS(new InvalidCredentialsError()),
    ACCOUNT_BLOCKED(new AuthenticationError()),
    SESSION_EXPIRED(new AuthenticationError());

    private AgentBankApiError error;

    public boolean isError() {
        return !this.equals(NO_ERRORS);
    }
}
