package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class MortgageLoanEntity {
    private String loanNumber;
    private String displayName;
    private Double amount;
    private String currency;
    private String amountTxt;
    private String propertyAddress;

    public String getLoanNumber() {
        return loanNumber;
    }

    public LoanAccount toTinkLoan(LoanDetailsResponse loanDetails) {
        return LoanAccount.builder(loanNumber, ExactCurrencyAmount.of(0 - amount, currency))
                .setAccountNumber(loanNumber)
                .setName(displayName)
                .setInterestRate(loanDetails.getInterestRate())
                .setDetails(
                        LoanDetails.builder(loanDetails.getType())
                                .setSecurity(propertyAddress)
                                .setNumMonthsBound(loanDetails.getNumOfMonthsBound())
                                .setInitialBalance(loanDetails.getInitialBalance())
                                .build())
                .build();
    }
}
