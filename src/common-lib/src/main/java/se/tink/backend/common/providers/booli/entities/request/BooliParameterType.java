package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;

public interface BooliParameterType {
    @JsonValue
    String value();
}
