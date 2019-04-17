package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankingTokenRequest {

    private String installationId;
    private String nonce;
    private String timestamp;

    public BankingTokenRequest(String installationId, String nonce, String timestamp) {
        this.installationId = installationId;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }
}
