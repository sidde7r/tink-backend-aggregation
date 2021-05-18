package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorResponse {
    private int httpStatus;
    private String error;
    private String errorDescription;

    @JsonIgnore
    public boolean isInvalidAccessToken() {
        return NordeaFIConstants.ErrorCodes.INVALID_TOKEN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidRefreshToken() {
        return NordeaFIConstants.ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoAgreement() {
        return NordeaFIConstants.ErrorCodes.AGREEMENT_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoClassification() {
        return NordeaFIConstants.ErrorCodes.CLASSIFICATION_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoConnectedAccount() {
        return NordeaFIConstants.ErrorCodes.UNABLE_TO_LOAD_CUSTOMER.equalsIgnoreCase(error);
    }
}
