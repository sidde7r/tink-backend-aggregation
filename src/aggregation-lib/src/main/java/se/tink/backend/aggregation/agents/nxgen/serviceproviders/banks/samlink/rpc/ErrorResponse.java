package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@JsonObject
public class ErrorResponse {
    private List<ErrorEntity> errors;

    @JsonIgnore
    public static ErrorResponse fromHttpResponseException(HttpResponseException exception) {
        return exception.getResponse().getBody(ErrorResponse.class);
    }

    @JsonIgnore
    public Optional<SamlinkConstants.ServerError> toUserError() {
        return errorsStream()
                .map(SamlinkConstants.ServerError::findServerError)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
    }

    public boolean hasError(SamlinkConstants.ServerError error) {
        return errorsStream()
                .map(ErrorEntity::getCode)
                .anyMatch(error::hasCode);
    }

    public List<String> getErrorCodes() {
        return errorsStream().map(ErrorEntity::getCode).collect(Collectors.toList());
    }

    private Stream<ErrorEntity> errorsStream() {
        return errors == null ? Stream.empty() : errors.stream();
    }
}
