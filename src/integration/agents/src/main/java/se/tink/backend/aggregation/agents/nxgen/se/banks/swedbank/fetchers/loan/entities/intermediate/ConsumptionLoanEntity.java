package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
                .setExactBalance(getAmount())
                .setName(getName())
                .setInterestRate(getInterest())
                .setDetails(buildLoanDetails(borrowers))
                .sourceInfo(buildSourceInfo())
                .build();
    }

    private LoanDetails buildLoanDetails(List<String> borrowers) {
        LoanDetails.Builder builder =
                LoanDetails.builder(
                                getName().contains(SwedbankSEConstants.MEMBERSHIP_LOAN)
                                        ? LoanDetails.Type.MEMBERSHIP
                                        : LoanDetails.Type.BLANCO)
                        .setApplicants(borrowers)
                        .setCoApplicant(borrowers.size() > 1);

        ExactCurrencyAmount monthlyAmortization = getMonthlyAmortization();

        if (monthlyAmortization != null) {
            builder.setMonthlyAmortization(monthlyAmortization);
        }

        return builder.build();
    }
}
