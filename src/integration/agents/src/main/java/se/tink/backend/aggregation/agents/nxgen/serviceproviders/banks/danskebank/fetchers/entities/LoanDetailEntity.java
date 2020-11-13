package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

// this class contains information about interest and som dates
// not decided on yet how we will use them
@JsonObject
public class LoanDetailEntity {
    private String percentageNumDecimalPlaces;
    private String taxDeductionForCapitalLosses;
    private String taxRate;
    private String lastLoanTermChangeDate;
    private String nextInterestFixDate;
    private String nextRefinancingDate;
    private String nextInterestAdjustmentDate;
    private String arpc;
    private String lastPaymentGross;
    private String possibleInterestOnlyMonthly;
    private String possibleInterestOnlyYearly;
    private String interestOnlyEndDate;
    private String interestOnlyStartDate;
    private String contributionPercentage;
    private String remainingLoanPeriodMonthly;
    private String remainingLoanPeriodYearly;
    private String dateOfDebt;
    private String cashDebt;
    private String bondDebt;
    private String principal;
    private String nextPaymentAfterTax;
    private String contribution;
    @Getter private String interest;
    private String instalment;
    private String paymentFrequency;
    private String nextPaymentAmount;
    private String nextPaymentDate;
    private String calculateMrk;
    private String agreementExpiryDate;
    private String flexLoanStatus;
    private String refinancingDate;
    private String debtAmount;
    private String loanTypeName;
}
