package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ChallengesEntity {
    @JsonProperty("wl_deviceAutoProvisioningRealm")
    private WlDeviceAutoProvisioningRealmEntity wlDeviceAutoProvisioningRealm;

    @JsonProperty("wl_authenticityRealm")
    private WlAuthenticityRealmEntity wlAuthenticityRealm;

    @JsonProperty("wl_antiXSRFRealm")
    private WlAntiXSRFRealmEntity wlAntiXSRFRealm;

    public WlDeviceAutoProvisioningRealmEntity getWlDeviceAutoProvisioningRealm() {
        return wlDeviceAutoProvisioningRealm;
    }

    public WlAuthenticityRealmEntity getWlAuthenticityRealm() {
        return wlAuthenticityRealm;
    }

    public WlAntiXSRFRealmEntity getWlAntiXSRFRealm() {
        return wlAntiXSRFRealm;
    }
}
