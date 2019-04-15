package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JWTPayload {
    private String sub;
    private String aud;
    private String exp;

    public JWTPayload(String sub, String aud, String exp) {
        this.sub = sub;
        this.aud = aud;
        this.exp = exp;
    }
}
