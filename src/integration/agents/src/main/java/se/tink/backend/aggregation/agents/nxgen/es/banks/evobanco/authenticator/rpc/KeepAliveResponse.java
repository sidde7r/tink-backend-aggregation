package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOSessionMaintainerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveResponse {
    @JsonProperty("EE_O_MantenimientoSesion")
    private EeOSessionMaintainerEntity eeOSessionMaintainerEntity;
}
