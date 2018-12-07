package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollateralEntity {
    @JsonProperty("municipality_name")
    private String municipalityName;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("designation")
    private String designation;

    public String getMunicipalityName() {
        return municipalityName;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getDesignation() {
        return designation;
    }
}
