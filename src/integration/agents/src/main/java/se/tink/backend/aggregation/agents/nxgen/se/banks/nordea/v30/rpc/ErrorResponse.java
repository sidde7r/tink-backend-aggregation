package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@JsonObject
public class ErrorResponse {
    @JsonProperty private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    /**
     * @throws HttpResponseException if the http response doesn't have application/json content type
     *     header
     */
    public static ErrorResponse of(HttpResponseException e) throws HttpResponseException {
        HttpResponse response = e.getResponse();
        if (Objects.nonNull(response)
                && MediaType.APPLICATION_JSON_TYPE.equals(response.getType())) {
            return response.getBody(ErrorResponse.class);
        }
        throw e;
    }

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

    @JsonIgnore
    public boolean isDuplicatePayment() {
        return NordeaSEConstants.ErrorCodes.DUPLICATE_PAYMENT.equalsIgnoreCase(errorDescription);
    }
}
