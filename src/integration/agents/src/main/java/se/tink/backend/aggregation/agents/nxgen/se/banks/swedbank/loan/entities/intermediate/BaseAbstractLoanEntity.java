package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.amount.Amount;

public abstract class BaseAbstractLoanEntity {
    protected final Optional<LoanDetailsAccountEntity> loanDetails;
    protected final Optional<DetailedLoanResponse> allLoanDetails;
    protected final LoanEntity loanOverview;

    protected BaseAbstractLoanEntity(LoanEntity loanOverview) {
        this.loanDetails = Optional.empty();
        this.allLoanDetails = Optional.empty();
        this.loanOverview = loanOverview;
    }

    protected BaseAbstractLoanEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        this.allLoanDetails = Optional.ofNullable(loanDetails);
        this.loanDetails = Optional.ofNullable(loanDetails.getLoanDetails());
        this.loanOverview = loanOverview;
    }

    public String getName() {
        return loanOverview.getName();
    }

    public String getAccountNumber() {
        return allLoanDetails
                .map(ld -> ld.getLoan().getAccount().getAccountNumber())
                .orElse(loanOverview.getAccount().getAccountNumber());
    }

    public Amount getAmount() {
        return loanOverview.getDebt().toTinkAmount();
    }

    public String getFullAccountNumber() {
        return allLoanDetails
                .map(ld -> ld.getLoan().getAccount().getFullyFormattedNumber())
                .orElse(loanOverview.getAccount().getFullyFormattedNumber());
    }

    public String getClearingNumber() {
        return allLoanDetails
                .map(ld -> ld.getLoan().getAccount().getClearingNumber())
                .orElse(loanOverview.getAccount().getClearingNumber());
    }

    public String getCurrency() {
        return loanOverview.getDebt().getCurrencyCode();
    }

    public abstract LoanAccount toTinkLoan();
}
