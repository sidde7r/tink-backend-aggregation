package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessFieldsEntity {

    @JsonProperty("USERNAME")
    private String username;

    @JsonProperty("PIN")
    private String pin;

    public AccessFieldsEntity(String username, String pin) {
        this.username = username;
        this.pin = pin;
    }
}
