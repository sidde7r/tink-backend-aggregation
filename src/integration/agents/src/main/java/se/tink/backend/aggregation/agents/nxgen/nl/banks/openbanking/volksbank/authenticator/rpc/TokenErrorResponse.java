package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ErrorDescriptions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenErrorResponse {
    private String error;
    private String errorDescription;

    @JsonIgnore
    public boolean isInvalidRequest() {
        return VolksbankConstants.ErrorCodes.INVALID_REQUEST.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isExpiredToken() {
        return isInvalidRequest()
                && ErrorDescriptions.EXPIRED_TOKEN.equalsIgnoreCase(errorDescription);
    }
}
