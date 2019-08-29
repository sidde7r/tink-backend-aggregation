package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaErrorResponse extends NordeaBaseResponse {
    private ErrorEntity error;

    public boolean isBankSideFailure() {

        return error.isBankSideFailure();
    }

    public void checkPisError(Throwable cause) throws PaymentException {
        if (error != null) {
            error.parseAndThrowPis(cause);
        } else {
            throw new PaymentException("Undefined PaymentException", cause);
        }
    }
}
