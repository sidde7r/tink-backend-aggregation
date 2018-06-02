package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountsEntity {
    private ExecutedEntity executed;

    public ExecutedEntity getExecuted() {
        return executed;
    }
}
