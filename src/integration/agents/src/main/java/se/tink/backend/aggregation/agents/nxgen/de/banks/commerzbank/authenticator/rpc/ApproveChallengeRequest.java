package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ApproveChallengeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApproveChallengeRequest {
    private ApproveChallengeEntity data;

    private ApproveChallengeRequest(String appId, String signature) {
        this.data = ApproveChallengeEntity.create(appId, signature);
    }

    public static ApproveChallengeRequest create(String appId, String signature) {
        return new ApproveChallengeRequest(appId, signature);
    }
}
