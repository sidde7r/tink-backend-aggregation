package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("error_description")
    private String errorDescription;

    private String error;

    public boolean isInvalidGrant() {
        return ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }
}
