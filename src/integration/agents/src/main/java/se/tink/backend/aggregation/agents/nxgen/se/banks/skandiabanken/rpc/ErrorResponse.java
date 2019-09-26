package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorMessages.ERROR_CODE_MISSING_CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorMessages.ERROR_MESSAGE_COMMUNICATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorMessages.STATUS_MESSAGE_INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("StatusCode")
    private int statusCode;

    @JsonProperty("StatusMessage")
    private String statusMessage = "";

    @JsonProperty("ErrorCode")
    private String errorCode = "";

    @JsonProperty("ErrorMessage")
    private String errorMessage = "";

    @JsonIgnore
    public boolean isUnauthorized() {
        return statusMessage.equalsIgnoreCase("Unauthorized");
    }

    public boolean isNoCreditCardsError() {
        if (statusCode == 500
                && STATUS_MESSAGE_INTERNAL_SERVER_ERROR.equalsIgnoreCase(statusMessage)
                && ERROR_CODE_MISSING_CREDIT_CARDS.equalsIgnoreCase(errorCode)
                && ERROR_MESSAGE_COMMUNICATION_FAILED.equalsIgnoreCase(errorMessage)) {
            return true;
        }
        return false;
    }
}
