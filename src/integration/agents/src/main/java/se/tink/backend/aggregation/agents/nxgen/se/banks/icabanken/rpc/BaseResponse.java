package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class BaseResponse<T> {
    @JsonProperty("Body")
    private T body;

    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }

    public T getBody() {
        return Optional.ofNullable(body)
                .orElseThrow(
                        () -> new IllegalStateException("Expected a body object but it was null"));
    }
}
