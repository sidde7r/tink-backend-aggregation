package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankingTokenRequestEntity {

    private String installationId;
    private String nonce;
    private String timestamp;

    public BankingTokenRequestEntity(String installationId, String nonce, String timestamp) {
        this.installationId = installationId;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }
}
