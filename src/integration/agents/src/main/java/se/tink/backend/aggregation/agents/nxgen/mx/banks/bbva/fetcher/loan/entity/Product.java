package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Product {
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
