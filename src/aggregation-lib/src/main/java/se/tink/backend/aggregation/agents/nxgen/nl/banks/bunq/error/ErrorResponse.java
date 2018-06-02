package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("Error")
    private List<ErrorEntity> error;

    public List<ErrorEntity> getError() {
        return error;
    }

    @JsonIgnore
    public Optional<String> getErrorDescription() {
        return Optional.ofNullable(error)
                .map(errorList -> errorList.stream().findFirst().map(ErrorEntity::getErrorDescription))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
