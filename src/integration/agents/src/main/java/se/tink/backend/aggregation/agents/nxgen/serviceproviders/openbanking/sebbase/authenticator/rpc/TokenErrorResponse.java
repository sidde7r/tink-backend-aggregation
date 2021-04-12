package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenErrorResponse {
    @JsonProperty("error_description")
    private String errorDescription;

    private String error;

    @JsonIgnore
    public boolean isInvalidGrant() {
        return ErrorMessages.INVALID_GRANT_ERROR.equalsIgnoreCase(error);
    }
}
