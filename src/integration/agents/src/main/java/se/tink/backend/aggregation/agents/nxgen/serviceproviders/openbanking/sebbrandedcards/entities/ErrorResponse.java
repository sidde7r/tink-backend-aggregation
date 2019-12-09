package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("error")
    private Error error;

    public Error getError() {
        return error;
    }

    public boolean isEndOfPagingError() {
        return getError()
                        .getErrorCode()
                        .equalsIgnoreCase(SebCommonConstants.ERROR.PAGINATING_ERROR_CODE)
                && getError()
                        .getDeveloperMessage()
                        .equalsIgnoreCase(SebCommonConstants.ERROR.PAGINATING_ERROR_MESSAGE);
    }
}
