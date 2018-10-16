package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity.TokenEntity;

public class TokenRequest {
    private TokenEntity tokenEntity;

    public TokenRequest(String grantType, String clientId, String clientSecret, String code, String redirectUri) {
        this.tokenEntity = new TokenEntity(grantType, clientId, clientSecret, code, redirectUri);
    }

    public String toData() {
        return tokenEntity.toForm();
    }

}
