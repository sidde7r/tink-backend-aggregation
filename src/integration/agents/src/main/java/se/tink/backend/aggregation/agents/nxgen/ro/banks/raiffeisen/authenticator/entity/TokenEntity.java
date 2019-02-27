package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenEntity {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String code;
    private String redirectUri;

    public TokenEntity(String grantType, String clientId, String clientSecret, String code, String redirectUri) {
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.code = code;
        this.redirectUri = redirectUri;
    }

    public String toForm() {
        return Form.builder()
                .put(RaiffeisenConstants.FORM.GRANT_TYPE, grantType)
                .put(RaiffeisenConstants.FORM.CLIENT_ID, clientId)
                .put(RaiffeisenConstants.FORM.CLIENT_SECRET, clientSecret)
                .put(RaiffeisenConstants.FORM.CODE, code)
                .put(RaiffeisenConstants.FORM.REDIRECT_URI, redirectUri)
                .build().serialize();
    }
}
