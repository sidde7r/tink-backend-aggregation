package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("error")
    private Error error;

    public Error getError() {
        return error;
    }

    public boolean isEndOfPagingError() {
        return getError().getErrorCode().equalsIgnoreCase(ErrorMessages.PAGINATING_ERROR_CODE)
                && getError()
                        .getDeveloperMessage()
                        .equalsIgnoreCase(ErrorMessages.PAGINATING_ERROR_MESSAGE);
    }
}
