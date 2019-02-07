package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class CollateralsLoanEntity extends BaseAbstractLoanDetailedEntity {

    private CollateralsLoanEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    private CollateralsLoanEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    protected static CollateralsLoanEntity create(LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
        return new CollateralsLoanEntity(loanDetails, loanOverview);
    }

    protected static CollateralsLoanEntity create(LoanEntity loanOverview) {
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
                .setDetails(buildLoanDetails(borrowers))
                .build();
    }

    private LoanDetails buildLoanDetails(List<String> borrowers) {
        return LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                .setNumMonthsBound(getNumMonthsBound())
                .setNextDayOfTermsChange(getNextDayOfTermsChange())
                .setMonthlyAmortization(getMonthlyAmortization())
                .setSecurity(getSecurity())
                .setApplicants(borrowers)
                .setCoApplicant(borrowers.size() > 1)
                .build();
    }


    protected int getNumMonthsBound() {
        return loanDetails.map(LoanDetailsAccountEntity::getFixedInterestPeriod)
                .map(SwedbankSeSerializationUtils::parseNumMonthsBound)
                .orElse(0);
    }

    protected Date getNextDayOfTermsChange() {
        return loanDetails.map(LoanDetailsAccountEntity::getInterestFixedToDate).orElse(null);
    }

    private String getSecurity() {
        return loanDetails.map(LoanDetailsAccountEntity::getCollaterals).orElseGet(Collections::emptyList)
                .stream().map(CollateralsEntity::getDescription)
                .collect(Collectors.joining(","));
    }
}
