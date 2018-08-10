package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

public class CollateralsLoanEntity extends BaseAbstractLoanDetailedEntity {

    protected CollateralsLoanEntity(
            LoanEntity loanOverview) {
        super(loanOverview);
    }

    protected CollateralsLoanEntity(
            DetailedLoanResponse loanDetails,
            LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    public static CollateralsLoanEntity create(LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new CollateralsLoanEntity(loanDetails, loanOverview);
    }

    public static CollateralsLoanEntity create(LoanEntity loanOverview) {
        return new CollateralsLoanEntity(loanOverview);
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
                                .setType(LoanDetails.Type.MORTGAGE)
                                .setMonthlyAmortization(getMonthlyAmortization())
                                .setSecurity(getSecurity())
                                .setApplicants(borrowers)
                                .setCoApplicant(borrowers.size() > 1 ? true : false)
                                .setName(getName())
                                .build()

                ).build();
    }

    private String getSecurity() {
        return loanDetails.map(ld -> ld.getCollaterals()).orElseGet(Collections::emptyList)
                .stream().map(c -> c.getDescription())
                .collect(Collectors.joining(","));
    }
}
