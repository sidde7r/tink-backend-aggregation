package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class ConsumptionLoanEntity extends BaseAbstractLoanDetailedEntity {

    private ConsumptionLoanEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    private ConsumptionLoanEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    protected static ConsumptionLoanEntity create(
            LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new ConsumptionLoanEntity(loanDetails, loanOverview);
    }

    protected static ConsumptionLoanEntity create(LoanEntity loanOverview) {
        return new ConsumptionLoanEntity(loanOverview);
    }

    @Override
    public LoanAccount toTinkLoan() {
        List<String> borrowers = getBorrowers();
        return LoanAccount.builder(getFullAccountNumber())
                .setAccountNumber(getAccountNumber())
                .setBalance(getAmount())
                .setName(getName())
                .setInterestRate(getInterest())
                .setDetails(buildLoanDetails(borrowers))
                .build();
    }

    private LoanDetails buildLoanDetails(List<String> borrowers) {
        return LoanDetails.builder(
                        getName().contains(SwedbankSEConstants.MEMBERSHIP_LOAN)
                                ? LoanDetails.Type.MEMBERSHIP
                                : LoanDetails.Type.BLANCO)
                .setMonthlyAmortization(getMonthlyAmortization())
                .setApplicants(borrowers)
                .setCoApplicant(borrowers.size() > 1)
                .build();
    }
}
