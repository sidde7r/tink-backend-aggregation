package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.AksjerOverview;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AksjerOverviewResponse {
    private AksjerOverview data;

    public AksjerOverview getData() {
        return data;
    }
}
