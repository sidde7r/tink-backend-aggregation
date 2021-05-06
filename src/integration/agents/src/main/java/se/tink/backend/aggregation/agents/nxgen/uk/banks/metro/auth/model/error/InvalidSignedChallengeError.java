package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error;

import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class InvalidSignedChallengeError extends AuthorizationError {
    private static final String ERROR_CODE = "MBE-2";
    private static final String MESSAGE = "Challenge signature is invalid or malformed.";

    public InvalidSignedChallengeError() {
        super(Error.builder().errorCode(ERROR_CODE).errorMessage(MESSAGE).build());
    }

    @Override
    public <T> T accept(BankApiErrorVisitor<? extends T> visitor) {
        return visitor.visit(this);
    }
}
