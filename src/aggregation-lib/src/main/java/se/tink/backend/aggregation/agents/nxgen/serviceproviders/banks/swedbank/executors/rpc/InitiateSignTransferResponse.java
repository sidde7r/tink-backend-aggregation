package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

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
