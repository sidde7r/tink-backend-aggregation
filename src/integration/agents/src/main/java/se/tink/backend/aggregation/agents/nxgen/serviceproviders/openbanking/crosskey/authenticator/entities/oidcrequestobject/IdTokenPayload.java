package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdTokenPayload {

    private String aud;
    private String openbankingIntentId;
    private String scope;
    private String iss;
    private Integer exp;
    private Integer iat;

    public String getOpenbankingIntentId() {
        return openbankingIntentId;
    }
}
