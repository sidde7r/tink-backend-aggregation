package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.FatalErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.ResponseError;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class BaseResponse {
    @JsonProperty("Error")
    private ErrorResponse error;

    public ErrorResponse getError() {
        return error;
    }

    @JsonIgnore
    public void checkForErrors() throws UserErrorException, FatalErrorException {
        ResponseError err = error.buildError();

        if (err != null) {
            if (err.getType().equals(ResponseError.Type.USER_ERROR)) {
                throw new UserErrorException(error.getMessage());
            }

            throw new FatalErrorException(error.getMessage());
        }
    }

    @JsonIgnore
    public boolean isBankIdAlreadyInProgress() {
        return error != null
                && IkanoApiConstants.ErrorCode.BANKID_IN_PROGRESS.equalsIgnoreCase(error.getCode());
    }

    @JsonIgnore
    public boolean isBankIdCancel() {
        return error != null
                && IkanoApiConstants.ErrorCode.BANKID_CANCELLED.equalsIgnoreCase(error.getCode());
    }
}
