package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientIdEntity {

    @JsonProperty("$ref")
    private String ref;

    public ClientIdEntity(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }
}
