package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class UnknownError extends AuthorizationError {
    private static final String ERROR_CODE = "MBE-1";
    private static final String MESSAGE =
            "Unknown error has appeared. Please handle it ! Details: Status: %s with body %s";

    public UnknownError(HttpStatus httpStatus, String body) {
        super(
                Error.builder()
                        .errorCode(ERROR_CODE)
                        .errorMessage(String.format(MESSAGE, httpStatus.value(), body))
                        .build());
    }

    @Override
    public <T> T accept(BankApiErrorVisitor<? extends T> visitor) {
        return visitor.visit(this);
    }
}
