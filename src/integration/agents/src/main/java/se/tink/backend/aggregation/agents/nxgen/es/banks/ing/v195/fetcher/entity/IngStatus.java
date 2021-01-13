package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class IngStatus {

    private String cod;
    private String description;

    public String getCod() {
        return cod;
    }

    public String getDescription() {
        return description;
    }
}
