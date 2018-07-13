package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.FatalErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.ResponseError;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseResponse {
    private ErrorResponse error;

    public ErrorResponse getError() {
        return error;
    }

    @JsonProperty("Error")
    public void setError(ErrorResponse error) {
        this.error = error;
    }

    public void checkForErrors() throws UserErrorException, FatalErrorException {
        ResponseError err = error.buildError();

        if (err != null) {
            if (err.getType().equals(ResponseError.Type.USER_ERROR)) {
                throw new UserErrorException(error.getMessage());
            }

            throw new FatalErrorException(error.getMessage());
        }
    }
}