package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ProfileKeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteAppRegistrationRequest {
    private String appId;
    private ProfileKeyEntity profilesKey;

    private CompleteAppRegistrationRequest(String appId) {
        this.appId = appId;
        this.profilesKey = ProfileKeyEntity.create();
    }

    public static CompleteAppRegistrationRequest create(String appId) {
        return new CompleteAppRegistrationRequest(appId);
    }
}
