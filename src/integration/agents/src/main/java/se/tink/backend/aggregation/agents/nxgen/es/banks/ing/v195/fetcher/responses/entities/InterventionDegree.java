package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterventionDegree {

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
