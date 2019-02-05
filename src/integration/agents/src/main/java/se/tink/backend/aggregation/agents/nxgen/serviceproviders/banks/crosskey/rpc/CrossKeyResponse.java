package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc;

import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import se.tink.backend.aggregation.agents.exceptions.AgentExceptionImpl;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.entities.ResponseMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CrossKeyResponse {

    private ResponseMessage status;
    private boolean approvalNeeded;
    private boolean amlAnswersNeeded;

    protected boolean hasAnyErrors(String... errorMessages) {
        return status.hasAnyErrors(errorMessages);
    }

    public boolean isFailure() {
        return !status.isSuccess();
    }

    public ResponseMessage getStatus() {
        return status;
    }

    public void setStatus(ResponseMessage status) {
        this.status = status;
    }

    public boolean isApprovalNeeded() {
        return approvalNeeded;
    }

    public void setApprovalNeeded(boolean approvalNeeded) {
        this.approvalNeeded = approvalNeeded;
    }

    public boolean isAmlAnswersNeeded() {
        return amlAnswersNeeded;
    }

    public void setAmlAnswersNeeded(boolean amlAnswersNeeded) {
        this.amlAnswersNeeded = amlAnswersNeeded;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }

    public void validate(Supplier<? extends IllegalArgumentException> unexpectedFailure)
            throws AuthenticationException, AuthorizationException {
        Optional<CrossKeyError> error = status.getErrors().stream()
                .map(errorTag -> CrossKeyMessage.find(errorTag, unexpectedFailure))
                .findFirst();

        if (error.isPresent()) { // can't throw non-RuntimeException from java.util.function.Consumer.
            CrossKeyError crossKeyError = error.get();
            // Have to satisfy method signature...
            AgentExceptionImpl exception = crossKeyError.getAgentError().exception(crossKeyError.getKey());
            if (exception instanceof AuthenticationException) {
                throw (AuthenticationException) exception;
            } else {
                throw (AuthorizationException) exception;
            }
        }

        //Don't know if there would ever be a status.success=false without a message.
        if (isFailure()) {
            throw unexpectedFailure.get();
        }
    }
}
