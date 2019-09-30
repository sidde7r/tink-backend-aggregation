package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.AppRegistrationDataMapEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateAppRegistrationRequest {
    private String appId;
    private AppRegistrationDataMapEntity appRegistrationDataMap;

    private UpdateAppRegistrationRequest(String appId) {
        this.appId = appId;
        this.appRegistrationDataMap = new AppRegistrationDataMapEntity();
    }

    public static UpdateAppRegistrationRequest create(String appId) {
        return new UpdateAppRegistrationRequest(appId);
    }
}
