package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IngInterventionDegree {

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
