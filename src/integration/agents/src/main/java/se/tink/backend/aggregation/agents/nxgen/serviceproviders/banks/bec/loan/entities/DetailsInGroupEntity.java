package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsInGroupEntity {
    private String detailName;
    private String detailValue;

    public String getDetailName() {
        return detailName;
    }

    public String getDetailValue() {
        return detailValue;
    }
}
