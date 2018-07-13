package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities.AksjerOverview;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AksjerOverviewResponse {
    private AksjerOverview data;

    public AksjerOverview getData() {
        return data;
    }
}


