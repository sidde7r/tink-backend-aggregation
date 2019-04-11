package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOSessionMaintainerEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveResponse implements EERpcResponse {
    @JsonProperty("EE_O_MantenimientoSesion")
    private EeOSessionMaintainerEntity eeOSessionMaintainerEntity;

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOSessionMaintainerEntity.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOSessionMaintainerEntity.getErrors();
    }
}
