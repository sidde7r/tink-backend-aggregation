package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountsEntity {
    private ExecutedEntity executed;

    public ExecutedEntity getExecuted() {
        return executed;
    }
}
