package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    private String loginContext;
    private String authLevel;
    private String authMethod;
    private String bankIdOrgId;

    public String getLoginContext() {
        return loginContext;
    }

    public String getAuthLevel() {
        return authLevel;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getBankIdOrgId() {
        return bankIdOrgId;
    }
}
