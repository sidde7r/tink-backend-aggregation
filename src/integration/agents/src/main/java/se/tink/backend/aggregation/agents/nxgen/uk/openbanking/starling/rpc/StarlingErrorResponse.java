package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.ErrorCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StarlingErrorResponse {
    @JsonProperty("error")
    private String errorCode;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonIgnore
    public boolean isInsufficientScope() {
        return Strings.nullToEmpty(errorCode).trim().equalsIgnoreCase(ErrorCode.INSUFFICIENT_SCOPE);
    }
}
