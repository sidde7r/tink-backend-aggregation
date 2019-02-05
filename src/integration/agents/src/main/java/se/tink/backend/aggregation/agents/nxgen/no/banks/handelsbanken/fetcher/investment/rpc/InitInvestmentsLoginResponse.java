package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitInvestmentsLoginResponse {
    private String so;

    public String getSo() {
        return so;
    }
}
