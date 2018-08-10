package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

public class ConspumptionLoanEntity extends BaseAbstractLoanDetailedEntity {

    public static final String MEMBERSHIP_LOAN = "Medlemslån";

    private ConspumptionLoanEntity(
            DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    public ConspumptionLoanEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    public static ConspumptionLoanEntity create(LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new ConspumptionLoanEntity(loanDetails, loanOverview);
    }

    public static ConspumptionLoanEntity create(LoanEntity loanOverview) {
        return new ConspumptionLoanEntity(loanOverview);
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
                                .setType(getName().contains(MEMBERSHIP_LOAN) ?
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
