package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class LoanDetailsEntity extends LoanOverviewEntity {
    private String customerName;
    private LoanDetailsSLoan loanS;
    private LoanDetailsCBSLoan loanCBS;

    public LoanAccount toTinkLoanAccount() {
        if (loanCBS == null && loanS == null) {
            throw new IllegalStateException("Unknown loan data received: " + SerializationUtils.serializeToString(this));
        }

        LoanAccount.Builder<?, ?> loanAccountBuilder = LoanAccount.builder(getLoanNumber(), Amount.inEUR(-getBalance()));
        loanAccountBuilder
                .setAccountNumber(getLoanNumber())
                .setName(getLoanName().getFi())
                .setBankIdentifier(getLoanNumber())
                .setInterestRate(getInterestRate());

        if (loanCBS != null) {
            return loanAccountBuilder
                    .setDetails(loanCBS.toTinkLoan(this))
                    .build();
        }

        return loanAccountBuilder
                .setDetails(loanS.toTinkLoan(this))
                .build();
    }

    public String getCustomerName() {
        return customerName;
    }

    public LoanDetailsSLoan getLoanS() {
        return loanS;
    }

    public LoanDetailsCBSLoan getLoanCBS() {
        return loanCBS;
    }
}
