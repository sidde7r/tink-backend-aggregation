package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.Error;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.GroupHeader;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("group_header")
    private GroupHeader groupHeader;

    @JsonProperty("_type")
    private String type;

    @JsonProperty("error")
    private Error error;

    public GroupHeader getGroupHeader() {
        return groupHeader;
    }

    public String getType() {
        return type;
    }

    public Error getError() {
        return error;
    }

    @JsonIgnore
    public boolean isSsnInvalidError() {
        if (error == null || error.getFailures().isEmpty()) {
            return false;
        }
        return error.getFailures().stream()
                .anyMatch(
                        failure ->
                                "error.validation".equals(failure.getCode())
                                        && "The SSN number must be a 12-digit string"
                                                .equalsIgnoreCase(failure.getDescription())
                                        && "psuId".equalsIgnoreCase(failure.getPath())
                                        && "Pattern".equalsIgnoreCase(failure.getType()));
    }
}
