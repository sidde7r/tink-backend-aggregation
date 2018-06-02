package se.tink.backend.connector.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.connector.exception.error.RequestError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintViolationErrorsResponse extends ErrorResponse {
    private final List<ConstraintViolationErrorResponse> errors;

    public ConstraintViolationErrorsResponse(List<ConstraintViolationErrorResponse> errorResponses) {
        super(RequestError.VALIDATION_FAILED.getErrorMessage());
        this.errors = errorResponses;
    }

    public List<ConstraintViolationErrorResponse> getErrors() {
        return errors;
    }
}
