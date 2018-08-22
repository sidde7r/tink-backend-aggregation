package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundHoldingPart {
    private String ipIdfr;

    public String getIdentifier() {
        return ipIdfr;
    }
}
