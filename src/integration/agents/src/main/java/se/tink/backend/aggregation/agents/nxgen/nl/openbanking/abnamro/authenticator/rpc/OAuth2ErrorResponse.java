package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class OAuth2ErrorResponse {

    private String error;

    @JsonIgnore
    public boolean isInvalidGrant() {
        return AbnAmroConstants.ErrorMessages.INVALID_GRANT.equalsIgnoreCase(error);
    }
}
