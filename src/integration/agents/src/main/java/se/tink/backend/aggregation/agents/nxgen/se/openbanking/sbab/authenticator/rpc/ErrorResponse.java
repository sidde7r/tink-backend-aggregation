package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty private String error;

    @JsonIgnore
    public void handleErrors() {
        if (Errors.UNAUTHORIZED_CLIENT.equalsIgnoreCase(error)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
