package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class OpenIdErrorResponse {

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonIgnore
    public boolean hasError(String error) {
        if (error == null) {
            return false;
        }
        return this.error.equals(error);
    }

    @JsonIgnore
    public boolean containsErrorDescription(String errorDescription) {
        return this.errorDescription.contains(errorDescription);
    }
}
