package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshEntity {
    private String grantType = RaiffeisenConstants.BODY.GRANT_TYPE_REFRESH_TOKEN;
    private String refreshToken;

    public RefreshEntity(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public String toForm() {
        return new Form.Builder()
                .put(RaiffeisenConstants.FORM.GRANT_TYPE, grantType)
                .put(RaiffeisenConstants.FORM.REFRESH_TOKEN, refreshToken)
                .build().serialize();
    }

}
