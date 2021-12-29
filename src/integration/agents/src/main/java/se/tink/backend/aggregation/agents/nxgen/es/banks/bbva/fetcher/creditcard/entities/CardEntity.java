package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {

    private String id;

    public String getId() {
        return id;
    }

    public CardEntity setId(String id) {
        this.id = id;
        return this;
    }
}
