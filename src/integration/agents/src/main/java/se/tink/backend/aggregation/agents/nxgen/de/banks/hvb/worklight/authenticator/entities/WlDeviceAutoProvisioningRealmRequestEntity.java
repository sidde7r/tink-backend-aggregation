package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import org.codehaus.jackson.annotate.JsonProperty;
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
