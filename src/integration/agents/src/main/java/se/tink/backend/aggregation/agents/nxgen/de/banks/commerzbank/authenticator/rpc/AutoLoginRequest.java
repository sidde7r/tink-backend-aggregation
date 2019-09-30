package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.AutoLoginEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AutoLoginRequest {
    private AutoLoginEntity data;

    private AutoLoginRequest(String userId, String pin, String appId) {
        this.data = AutoLoginEntity.create(userId, pin, appId);
    }

    public static AutoLoginRequest create(String userId, String pin, String appId) {
        return new AutoLoginRequest(userId, pin, appId);
    }
}
