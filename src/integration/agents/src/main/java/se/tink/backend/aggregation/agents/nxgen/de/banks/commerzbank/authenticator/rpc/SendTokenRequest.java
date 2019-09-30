package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SendTokenRequest {
    private TokenEntity data;

    private SendTokenRequest(String appId, String b64EncodedPublicKey) {
        this.data = TokenEntity.create(appId, b64EncodedPublicKey);
    }

    public static SendTokenRequest create(String appId, String b64EncodedPublicKey) {
        return new SendTokenRequest(appId, b64EncodedPublicKey);
    }
}
