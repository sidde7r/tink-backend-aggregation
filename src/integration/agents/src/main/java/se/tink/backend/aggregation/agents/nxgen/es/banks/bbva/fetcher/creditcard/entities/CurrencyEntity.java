package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyEntity {
    private String id;

    public String getId() {
        return id;
    }
}
