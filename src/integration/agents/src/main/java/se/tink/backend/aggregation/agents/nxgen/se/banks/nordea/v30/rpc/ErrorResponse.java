package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonIgnore
    public boolean isInvalidAccessToken() {
        return NordeaSEConstants.ErrorCodes.INVALID_TOKEN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean tokenRequired() {
        return NordeaSEConstants.ErrorCodes.TOKEN_REQUIRED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidRefreshToken() {
        return NordeaSEConstants.ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoAgreement() {
        return NordeaSEConstants.ErrorCodes.AGREEMENT_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoClassification() {
        return NordeaSEConstants.ErrorCodes.CLASSIFICATION_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoConnectedAccount() {
        return NordeaSEConstants.ErrorCodes.UNABLE_TO_LOAD_CUSTOMER.equalsIgnoreCase(error);
    }
}
