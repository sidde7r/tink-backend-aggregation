package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MoreDetailsEntity {
    private String debtorRate;
    private String interestRate;
    private String administrationMarginRate;
    private String principalAmount;
    private String longLoanName;
    private String shortLoanName;
    private TaxEntity tax;
}
