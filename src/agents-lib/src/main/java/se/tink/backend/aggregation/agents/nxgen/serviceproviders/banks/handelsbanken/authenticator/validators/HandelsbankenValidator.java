package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public abstract class HandelsbankenValidator<A extends BaseResponse> {
    private final A response;

    public HandelsbankenValidator(A response) {
        this.response = response;
    }

    protected List<ErrorResponse> getErrors() {
        return response.getErrors();
    }

    public String getMessage() {
        return response.getMessage();
    }

    public String getCode() {
        return response.getCode();
    }

    protected Optional<String> getFirstErrorMessage() {
        return response.getFirstErrorMessage();
    }

    public String getResult() {
        return response.getResult();
    }

    protected A getResponse() {
        return response;
    }
}
