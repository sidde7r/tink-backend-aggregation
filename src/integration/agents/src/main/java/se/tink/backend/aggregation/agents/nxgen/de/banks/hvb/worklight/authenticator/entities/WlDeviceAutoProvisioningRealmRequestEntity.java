package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class WlDeviceAutoProvisioningRealmRequestEntity {
    @JsonProperty("ID")
    private String ID;

    @JsonProperty("ID")
    public void setID(String ID) {
        this.ID = ID;
    }
}
