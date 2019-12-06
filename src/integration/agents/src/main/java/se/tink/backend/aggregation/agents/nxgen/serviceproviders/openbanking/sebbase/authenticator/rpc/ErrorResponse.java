package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("error_description")
    private String errorDescription;

    private String error;

    @JsonIgnore
    public boolean isInvalidGrant() {
        return SebCommonConstants.ERROR.INVALID_GRANT_ERROR.equalsIgnoreCase(error);
    }
}
