package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccruedEntity {
    private String accruedCreditInterestAmount;
    private String accruedCreditInterestAmountAdjusted;
    private String accruedCreditInterestIndicator;
    private String accruedDebitInterestAmount;
    private String accruedDebitInterestAmountAdjusted;
    private String accruedDebitInterestIndicator;
    private String accruedPenaltyInterestAmount;
    private String accruedPenaltyInterestAmountAdjusted;
    private String accruedPenaltyInterestAmountAdjustedLP;
    private String accruedPenaltyInterestAmountLP;
    private String interestDate;
    private String pdAccountability;
}
