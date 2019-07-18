package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtTokenPayload implements JwtPayload {

    private String iss;
    private String sub;
    private long exp;
    private long iat;
    private String jti;
    private String aud;

    public JwtTokenPayload(String iss, String sub, long exp, long iat, String jti, String aud) {
        this.iss = iss;
        this.sub = sub;
        this.exp = exp;
        this.iat = iat;
        this.jti = jti;
        this.aud = aud;
    }
}
