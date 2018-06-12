package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities.AvailableBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AvailableBalanceResponse {
    private AvailableBalanceEntity data;

    public AvailableBalanceEntity getData() {
        return data;
    }
}
