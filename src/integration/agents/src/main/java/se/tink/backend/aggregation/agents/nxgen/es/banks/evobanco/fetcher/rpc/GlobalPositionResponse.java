package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.EeOGlobalbePositionEntity;

public class GlobalPositionResponse {
    @JsonProperty("EE_O_PosicionGlobalBE")
    private EeOGlobalbePositionEntity eeOGlobalbePosition;

    public EeOGlobalbePositionEntity getEeOGlobalbePosition() {
        return eeOGlobalbePosition;
    }
}
