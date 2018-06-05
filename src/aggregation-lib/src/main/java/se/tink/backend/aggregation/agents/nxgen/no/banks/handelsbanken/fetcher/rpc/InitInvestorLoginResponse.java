package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitInvestorLoginResponse {
    private String so;

    public String getSo() {
        return so;
    }
}
