package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.CustomerPropertiesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanEntity {
    @JsonProperty("number_of_borrowers")
    private int numberOfBorrowers;

    @JsonProperty("loan_status")
    private String loanStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("disbursement_date")
    private Date disbursementDate;

    @JsonProperty("initial_loan_amount")
    private long initialLoanAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("closed_date")
    private Date closedDate;

    @JsonProperty("loan_number")
    private String loanNumber;

    @JsonProperty("customer_properties")
    private CustomerPropertiesEntity customerProperties;

    @JsonProperty("collateral")
    private CollateralEntity collateral;

    @JsonProperty("loan_type")
    private LoanTypeEntity loanType;

    @JsonProperty("loan_terms")
    private LoanTermsEntity loanTerms;

    @JsonProperty("current_loan_amount")
    private long currentLoanAmount;

    public int getNumberOfBorrowers() {
        return numberOfBorrowers;
    }

    public String getLoanStatus() {
        return loanStatus;
    }

    public Date getDisbursementDate() {
        return disbursementDate;
    }

    public long getInitialLoanAmount() {
        return initialLoanAmount;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public CustomerPropertiesEntity getCustomerProperties() {
        return customerProperties;
    }

    public CollateralEntity getCollateral() {
        return collateral;
    }

    public LoanTypeEntity getLoanType() {
        return loanType;
    }

    public LoanTermsEntity getLoanTerms() {
        return loanTerms;
    }

    public long getCurrentLoanAmount() {
        return currentLoanAmount;
    }

    @JsonIgnore
    public LoanDetails toTinkLoanDetails() {
        return LoanDetails.builder(loanType.toTinkLoanType())
                .setAmortized(Amount.inSEK(initialLoanAmount - currentLoanAmount))
                .setMonthlyAmortization(Amount.inSEK(loanTerms.getAmortisationAmount()))
                .setInitialBalance(Amount.inSEK(initialLoanAmount).negate())
                .setInitialDate(disbursementDate)
                .setLoanNumber(loanNumber)
                .setNumMonthsBound(loanTerms.getBindingTimeMonths())
                .setNextDayOfTermsChange(loanTerms.getInterestRateAdjustmentDate())
                .setSecurity(collateral.getObjectType())
                .setCoApplicant(numberOfBorrowers > 1)
                .build();
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        final Double interestRate =
                Optional.ofNullable(loanTerms).map(LoanTermsEntity::getInterestRate).orElse(null);

        return LoanAccount.builder(loanNumber, Amount.inSEK(currentLoanAmount).negate())
                .setName(loanNumber)
                .setAccountNumber(loanNumber)
                .setBankIdentifier(loanNumber)
                .setInterestRate(interestRate)
                .setDetails(toTinkLoanDetails())
                .build();
    }
}
