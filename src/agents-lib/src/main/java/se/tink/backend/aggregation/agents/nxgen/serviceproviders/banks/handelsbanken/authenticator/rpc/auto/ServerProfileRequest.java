package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServerProfileRequest {

    private String encUserCredentials;
    private String profileId;

    public ServerProfileRequest setEncUserCredentials(String encUserCredentials) {
        this.encUserCredentials = encUserCredentials;
        return this;
    }

    public ServerProfileRequest setProfileId(String profileId) {
        this.profileId = profileId;
        return this;
    }
}
