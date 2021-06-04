package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class EvoBancoError {

    @JsonProperty("codigo")
    private String code;

    private String message;
}
