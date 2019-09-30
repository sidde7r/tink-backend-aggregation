package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {
    private String appId;
    private String publicKeyX509;

    private TokenEntity(String appId, String publicKey) {
        this.appId = appId;
        this.publicKeyX509 = publicKey;
    }

    public static TokenEntity create(String appId, String publickKey) {
        return new TokenEntity(appId, publickKey);
    }
}
