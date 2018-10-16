package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity.RefreshEntity;


public class RefreshRequest {
    private RefreshEntity refreshEntity;

    public RefreshRequest(String refreshToken) {
        this.refreshEntity = new RefreshEntity(refreshToken);
    }

    public String toTinkRefresh() {
        return refreshEntity.toForm();
    }
}
