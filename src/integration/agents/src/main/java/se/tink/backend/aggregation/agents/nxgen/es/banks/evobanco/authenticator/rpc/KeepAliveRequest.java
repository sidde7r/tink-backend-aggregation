package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeISessionMaintainerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveRequest {
    @JsonProperty("EE_I_MantenimientoSesion")
    private EeISessionMaintainerEntity eeISessionMaintainerEntity;

    @JsonIgnore
    public KeepAliveRequest(EeISessionMaintainerEntity eeISessionMaintainerEntity) {
        this.eeISessionMaintainerEntity = eeISessionMaintainerEntity;
    }
}
