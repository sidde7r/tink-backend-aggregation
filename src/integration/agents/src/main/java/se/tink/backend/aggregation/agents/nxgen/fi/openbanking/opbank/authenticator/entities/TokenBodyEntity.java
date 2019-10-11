package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenBodyEntity {
    private String aud;
    private String iss;
    private String response_type;
    private String client_id;
    private String redirect_uri;
    private String scope;
    private String state;
    private String nonce;
    private long max_age;
    private long exp;
    private long iat;
    private ClaimsEntity claims;

    public void setAud(String aud) {
        this.aud = aud;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public void setResponseType(String response_type) {
        this.response_type = response_type;
    }

    public void setClientId(String client_id) {
        this.client_id = client_id;
    }

    public void setRedirectUri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setMaxAge(long max_age) {
        this.max_age = max_age;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public void setClaims(ClaimsEntity claims) {
        this.claims = claims;
    }
}
