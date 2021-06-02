package se.tink.backend.aggregation.agents.banks.sbab.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansEntity {

    private MortgagesEntity mortgages;

    public MortgagesEntity getMortgages() {
        return mortgages;
    }
}
