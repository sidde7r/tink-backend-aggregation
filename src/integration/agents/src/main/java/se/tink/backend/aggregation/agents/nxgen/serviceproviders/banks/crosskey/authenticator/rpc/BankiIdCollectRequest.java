package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankiIdCollectRequest {
    private String appVersion;

    public BankiIdCollectRequest(String appVersion) {
        this.appVersion = appVersion;
    }
}
