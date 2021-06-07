package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class EvoBancoErrorResponse {
    private EvoBancoError response;
}
