package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApproveChallengeEntity {
    private String appId;
    private String signature;

    private ApproveChallengeEntity(String appId, String signature) {
        this.appId = appId;
        this.signature = signature;
    }

    public static ApproveChallengeEntity create(String appId, String signature) {
        return new ApproveChallengeEntity(appId, signature);
    }
}
