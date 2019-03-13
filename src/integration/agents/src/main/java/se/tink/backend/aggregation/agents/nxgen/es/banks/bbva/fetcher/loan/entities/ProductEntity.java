package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String id;

    public String getId() {
        return id;
    }
}
