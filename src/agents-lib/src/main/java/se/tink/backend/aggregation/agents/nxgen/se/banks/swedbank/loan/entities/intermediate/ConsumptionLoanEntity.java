package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

public class ConsumptionLoanEntity extends BaseAbstractLoanDetailedEntity {


    private ConsumptionLoanEntity(
            DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    public ConsumptionLoanEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    public static ConsumptionLoanEntity create(LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new ConsumptionLoanEntity(loanDetails, loanOverview);
    }

    public static ConsumptionLoanEntity create(LoanEntity loanOverview) {
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
                .setDetails(
                        LoanDetails.builder()
                                .setType(getName().contains(SwedbankSEConstants.MEMBERSHIP_LOAN) ?
                                        LoanDetails.Type.MEMBERSHIP :
                                        LoanDetails.Type.BLANCO)
                                .setMonthlyAmortization(getMonthlyAmortization())
                                .setApplicants(borrowers)
                                .setCoApplicant(borrowers.size() > 1 ? true : false)
                                .setName(getName())
                                .build()

                ).build();
    }
}
