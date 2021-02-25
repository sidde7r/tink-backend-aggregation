package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CajamarErrorResponse {
    private String code;
    private String message;
}
