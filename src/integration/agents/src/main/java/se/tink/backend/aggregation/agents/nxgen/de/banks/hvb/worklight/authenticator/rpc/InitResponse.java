package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.ChallengesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class InitResponse {
    @JsonProperty("challenges")
    private ChallengesEntity challenges;

    public String getWlInstanceId() {
        return challenges.getWlAntiXSRFRealm().getwLInstanceId();
    }

    public String getToken() {
        return challenges.getWlDeviceAutoProvisioningRealm().getID().getToken();
    }

    public String getWlChallengeData() {
        return challenges.getWlAuthenticityRealm().getwLChallengeData();
    }

    public boolean getAllowed() {
        return challenges.getWlDeviceAutoProvisioningRealm().getID().getAllowed();
    }

    public String getCertificate() {
        return challenges.getWlDeviceAutoProvisioningRealm().getCertificate();
    }
}
