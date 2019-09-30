package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAppRegistrationEntity {
    private String appId;

    public String getAppId() {
        return appId;
    }
}
