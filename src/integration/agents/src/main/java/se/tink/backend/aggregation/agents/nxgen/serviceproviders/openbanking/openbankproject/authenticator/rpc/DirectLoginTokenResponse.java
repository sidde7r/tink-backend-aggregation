package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DirectLoginTokenResponse {

    private String token;

    public String getToken() {
        return token;
    }
}
