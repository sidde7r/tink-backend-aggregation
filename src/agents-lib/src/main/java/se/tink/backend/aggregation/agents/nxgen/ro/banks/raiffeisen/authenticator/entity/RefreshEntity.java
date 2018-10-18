package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshEntity {
    private String grantType = RaiffeisenConstants.BODY.GRANT_TYPE_REFRESH_TOKEN;
    private String refreshToken;
    private String clientId;
    private String clientSecret;

    public RefreshEntity(String refreshToken, String clientId, String clientSecret) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public String toForm() {
        return new Form.Builder()
                .put(RaiffeisenConstants.FORM.GRANT_TYPE, grantType)
                .put(RaiffeisenConstants.FORM.REFRESH_TOKEN, refreshToken)
                .put(RaiffeisenConstants.FORM.CLIENT_ID, clientId)
                .put(RaiffeisenConstants.FORM.CLIENT_SECRET, clientSecret)
                .build().serialize();
    }

}
