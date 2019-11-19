package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BodyEntity {
    public SessionEntity getSession() {
        return session;
    }

    public ClientEntity getClient() {
        return client;
    }

    public DeviceEntity getDevice() {
        return device;
    }

    @JsonProperty("Session")
    private SessionEntity session;

    @JsonProperty("Client")
    private ClientEntity client;

    @JsonProperty("Device")
    private DeviceEntity device;
}
