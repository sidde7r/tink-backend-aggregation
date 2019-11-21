package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private String error;

    @JsonIgnore
    public boolean isInvalidGrant() {
        return IngBaseConstants.ErrorMessages.INVALID_GRANT_ERROR.equalsIgnoreCase(error);
    }
}
