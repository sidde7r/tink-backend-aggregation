package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest {
    private String languageCode;
    private String realEstateNumber;
    private String loanNumber;

    public LoanDetailsRequest(String languageCode, String realEstateNumber, String loanNumber) {
        this.languageCode = languageCode;
        this.realEstateNumber = realEstateNumber;
        this.loanNumber = loanNumber;
    }
}
