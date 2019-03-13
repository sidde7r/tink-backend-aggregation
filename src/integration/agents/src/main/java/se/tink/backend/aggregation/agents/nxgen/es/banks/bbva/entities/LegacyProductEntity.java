package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LegacyProductEntity {
    private String offer;
    private String product;

    public String getOffer() {
        return offer;
    }

    public String getProduct() {
        return product;
    }
}
