package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanEntity {
    private String realEstateNumber;
    private String loanNumber;

    public String getRealEstateNumber() {
        return realEstateNumber;
    }

    public String getLoanNumber() {
        return loanNumber;
    }
}
