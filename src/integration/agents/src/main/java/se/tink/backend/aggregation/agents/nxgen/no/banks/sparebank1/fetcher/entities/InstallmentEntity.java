package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstallmentEntity {
    private String dueDate;
    private String amountInteger;
    private String amountFraction;
    private String interestAmountInteger;
    private String interestAmountFraction;
    private String costsInteger;
    private String costsFraction;
    private String balanceInteger;
    private String balanceFraction;
    private String principalInteger;
    private String principalFraction;

    public String getDueDate() {
        return dueDate;
    }

    public String getAmountInteger() {
        return amountInteger;
    }

    public String getAmountFraction() {
        return amountFraction;
    }

    public String getInterestAmountInteger() {
        return interestAmountInteger;
    }

    public String getInterestAmountFraction() {
        return interestAmountFraction;
    }

    public String getCostsInteger() {
        return costsInteger;
    }

    public String getCostsFraction() {
        return costsFraction;
    }

    public String getBalanceInteger() {
        return balanceInteger;
    }

    public String getBalanceFraction() {
        return balanceFraction;
    }

    public String getPrincipalInteger() {
        return principalInteger;
    }

    public String getPrincipalFraction() {
        return principalFraction;
    }
}
