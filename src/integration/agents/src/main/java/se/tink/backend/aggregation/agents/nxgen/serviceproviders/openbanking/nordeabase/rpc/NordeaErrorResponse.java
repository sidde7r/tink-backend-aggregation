package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaErrorResponse extends NordeaBaseResponse {
    private ErrorEntity error;

    @JsonIgnore
    public boolean isBankSideFailure() {
        return error != null && error.isBankSideFailure();
    }

    @JsonIgnore
    public boolean isConsentNotFound() {
        return error != null && error.isConsentNotFound();
    }

    @JsonIgnore
    public boolean isFetchCertificateFailure() {
        return error != null && error.isFetchCertificateFailure();
    }

    @JsonIgnore
    public boolean isRefreshTokenInvalid() {
        return error != null && error.isRefreshTokenInvalid();
    }

    @JsonIgnore
    public void checkPisError(Throwable cause) throws PaymentException {
        if (error != null) {
            error.parseAndThrowPis(cause);
        } else {
            throw new PaymentException("Undefined PaymentException", cause);
        }
    }
}
