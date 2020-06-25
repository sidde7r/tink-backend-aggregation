package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanObjectEntity {
    private String designation;
    private String municipalityName;
    private String objectId;

    public String getDesignation() {
        return designation;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public String getObjectId() {
        return objectId;
    }
}
