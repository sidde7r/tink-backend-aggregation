package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("error_code")
    private String errorCode;

    private String error;

    private String url;

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public String getUrl() {
        return url;
    }
}
