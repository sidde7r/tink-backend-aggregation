package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ErrorResponse {
    private int statusCode;
    private String statusMessage = "";
    private String errorCode = "";
    private String errorMessage = "";

    @JsonIgnore
    public boolean isUnauthorized() {
        return statusMessage.equalsIgnoreCase("Unauthorized");
    }

    @JsonIgnore
    public boolean isInvalidPaymentDate() {
        return ErrorCodes.INVALID_PAYMENT_DATE.equalsIgnoreCase(errorCode);
    }
}
