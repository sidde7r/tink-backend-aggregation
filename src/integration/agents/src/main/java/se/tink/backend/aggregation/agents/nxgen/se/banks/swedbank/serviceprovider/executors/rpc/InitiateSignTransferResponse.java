package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSignTransferResponse extends AbstractBankIdSignResponse {
    private String autoStartToken;
    private String signingData;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getSigningData() {
        return signingData;
    }
}
