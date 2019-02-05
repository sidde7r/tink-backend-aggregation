package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditLineDetailsEntity {
    private BalanceEntity creditLine;
    private String creditType;
    private String interestRateExistingCreditLine;
    private String interestRateToleratedOverdraft;
    private String validUntil;

    public BalanceEntity getCreditLine() {
        return creditLine;
    }

    public String getCreditType() {
        return creditType;
    }

    public String getInterestRateExistingCreditLine() {
        return interestRateExistingCreditLine;
    }

    public String getInterestRateToleratedOverdraft() {
        return interestRateToleratedOverdraft;
    }

    public String getValidUntil() {
        return validUntil;
    }
}
