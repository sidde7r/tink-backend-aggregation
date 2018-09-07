package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

public class CarLoanEntity extends BaseAbstractLoanEntity {

    private CarLoanEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    private CarLoanEntity(LoanEntity loan) {
        super(loan);
    }

    protected static CarLoanEntity create(LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new CarLoanEntity(loanDetails, loanOverview);
    }

    protected static CarLoanEntity create(LoanEntity loan) {
        return new CarLoanEntity(loan);
    }

    public LoanAccount toTinkLoan() {
        return LoanAccount.builder(getFullAccountNumber())
                .setName(getName())
                .setBalance(getAmount())
                .setAccountNumber(getAccountNumber())
                .setDetails(buildLoanDetails())
                .build();
    }

    private LoanDetails buildLoanDetails() {
        return LoanDetails.builder(LoanDetails.Type.VEHICLE)
                .build();
    }
}
