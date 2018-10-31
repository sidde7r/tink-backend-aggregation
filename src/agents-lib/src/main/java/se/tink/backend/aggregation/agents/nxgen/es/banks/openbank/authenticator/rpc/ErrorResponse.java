package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public static ErrorResponse empty() {
        return new ErrorResponse();
    }

    public boolean hasError() {
        return Objects.nonNull(error) && !Strings.isNullOrEmpty(error);
    }

    public boolean hasErrorCode(String errorCode) {
        return Objects.nonNull(error) && error.equalsIgnoreCase(errorCode);
    }

    public boolean hasErrorDescription() {
        return Objects.nonNull(error) && !Strings.isNullOrEmpty(error);
    }

    public Optional<String> getError() {
        if (!hasError()) {
            return Optional.empty();
        }

        return Optional.of(error);
    }

    public Optional<String> getErrorDescription() {
        if (!hasErrorDescription()) {
            return Optional.empty();
        }

        return Optional.of(errorDescription);
    }
}
