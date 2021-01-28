package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
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
        return LoanAccount.nxBuilder()
                .withLoanDetails(createLoanModule(loanDetails))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanNumber)
                                .withAccountNumber(loanNumber)
                                .withAccountName(Optional.ofNullable(displayName).orElse(""))
                                .addIdentifier(new DanishIdentifier(loanNumber))
                                .setProductName(displayName)
                                .build())
                .setApiIdentifier(loanNumber)
                .build();
    }

    private LoanModule createLoanModule(LoanDetailsResponse loanDetails) {
        return LoanModule.builder()
                .withType(loanDetails.getType())
                .withBalance(ExactCurrencyAmount.of(0 - amount, currency))
                .withInterestRate(loanDetails.getInterestRate())
                .setAmortized(null)
                .setInitialBalance(loanDetails.getInitialBalance())
                .setApplicants(Collections.emptyList())
                .setCoApplicant(false)
                .setLoanNumber(loanNumber)
                .setNextDayOfTermsChange(null)
                .setSecurity(propertyAddress)
                .setNumMonthsBound(loanDetails.getNumOfMonthsBound())
                .build();
    }
}
