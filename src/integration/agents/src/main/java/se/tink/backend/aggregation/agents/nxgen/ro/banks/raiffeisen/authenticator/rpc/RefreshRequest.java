package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity.RefreshEntity;

public class RefreshRequest {
    private RefreshEntity refreshEntity;

    public RefreshRequest(
            String refreshToken, String clientId, String clientSecret, String redirectUrl) {
        this.refreshEntity = new RefreshEntity(refreshToken, clientId, clientSecret, redirectUrl);
    }

    public String toBody() {
        return refreshEntity.toForm();
    }
}
