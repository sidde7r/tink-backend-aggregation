package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaErrorResponse extends NordeaBaseResponse {
    private ErrorEntity error;

    public void checkError(Throwable cause) throws PaymentException {
        if (error != null) {
            error.parseAndThrow(cause);
        } else {
            throw new PaymentException("Undefined PaymentException", cause);
        }
    }
}
