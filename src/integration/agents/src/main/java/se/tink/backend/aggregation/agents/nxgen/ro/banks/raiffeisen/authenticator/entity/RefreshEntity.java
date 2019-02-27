package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshEntity {
    private String grantType = RaiffeisenConstants.BODY.GRANT_TYPE_REFRESH_TOKEN;
    private String refreshToken;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;

    public RefreshEntity(String refreshToken, String clientId, String clientSecret, String redirectUrl) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
    }


    public String toForm() {
        return Form.builder()
                .put(RaiffeisenConstants.FORM.GRANT_TYPE, grantType)
                .put(RaiffeisenConstants.FORM.REFRESH_TOKEN, refreshToken)
                .put(RaiffeisenConstants.FORM.CLIENT_ID, clientId)
                .put(RaiffeisenConstants.FORM.CLIENT_SECRET, clientSecret)
                .put(RaiffeisenConstants.FORM.REDIRECT_URI, redirectUrl)
                .build().serialize();
    }

}
