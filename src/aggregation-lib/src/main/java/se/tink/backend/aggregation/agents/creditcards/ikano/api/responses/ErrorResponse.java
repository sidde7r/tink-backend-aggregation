package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.ResponseError;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private String code;
    private String message;

    public ResponseError buildError() {
        ResponseError error = null;

        if (!code.equals("NO_ERROR")) {
            error = new ResponseError();
            error.setCode(code);
            error.setMessage(message);
        }

        return error;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @JsonProperty("ErrorCode")
    public void setCode(String errorCode) {
        this.code = errorCode.toUpperCase();
    }

    @JsonProperty("ErrorMessage")
    public void setMessage(String errorMessage) {
        this.message = errorMessage;
    }
}
