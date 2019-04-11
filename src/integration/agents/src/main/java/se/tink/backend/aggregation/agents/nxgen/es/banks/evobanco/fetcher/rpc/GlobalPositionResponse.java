package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.EeOGlobalbePositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;

public class GlobalPositionResponse implements EERpcResponse {
    @JsonProperty("EE_O_PosicionGlobalBE")
    private EeOGlobalbePositionEntity eeOGlobalbePosition;

    public EeOGlobalbePositionEntity getEeOGlobalbePosition() {
        return eeOGlobalbePosition;
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOGlobalbePosition.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOGlobalbePosition.getErrors();
    }
}
