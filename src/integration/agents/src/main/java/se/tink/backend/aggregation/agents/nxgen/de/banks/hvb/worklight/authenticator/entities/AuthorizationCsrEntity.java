package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AuthorizationCsrEntity {
    @JsonProperty private WlDeviceAutoProvisioningRealmEntity wl_deviceAutoProvisioningRealm;

    public AuthorizationCsrEntity(
            final WlDeviceAutoProvisioningRealmEntity wl_deviceAutoProvisioningRealm) {
        this.wl_deviceAutoProvisioningRealm = wl_deviceAutoProvisioningRealm;
    }
}
