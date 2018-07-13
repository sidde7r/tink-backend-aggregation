package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoTokenResponse {
    private String so;

    public String getSo() {
        return so;
    }
}
