package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecutedEntity {
    private Double value;
    private String currency;

    public Double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
