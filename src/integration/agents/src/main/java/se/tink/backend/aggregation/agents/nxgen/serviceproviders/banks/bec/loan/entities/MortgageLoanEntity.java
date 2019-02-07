package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

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

    public String getDisplayName() {
        return displayName;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmountTxt() {
        return amountTxt;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public LoanAccount toTinkLoan(LoanDetailsResponse loanDetails) {
        return LoanAccount.builder(getLoanNumber(), new Amount(currency, 0 - amount))
                .setAccountNumber(getLoanNumber())
                .setName(getDisplayName())
                .setInterestRate(loanDetails.getInterestRate().orElse(null))
                .setDetails(LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                        .setSecurity(getPropertyAddress())
                        .build())
                .build();
    }
}
