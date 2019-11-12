package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CredentialsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyCardLoginRequest {
    @JsonProperty private CredentialsEntity credentials = new CredentialsEntity();
    @JsonProperty private String hardwareId;

    @JsonIgnore
    public void setPin(String pin) {
        credentials.setPin(pin);
    }

    @JsonIgnore
    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }
}
