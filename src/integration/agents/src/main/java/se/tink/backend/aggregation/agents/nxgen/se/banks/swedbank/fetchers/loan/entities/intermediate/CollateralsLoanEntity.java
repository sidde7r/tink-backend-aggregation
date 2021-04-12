package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CollateralsLoanEntity extends BaseAbstractLoanDetailedEntity {

    private CollateralsLoanEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    private CollateralsLoanEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    protected static CollateralsLoanEntity create(
            LoanEntity loanOverview, DetailedLoanResponse loanDetails) {
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
                .setExactBalance(getAmount())
                .setName(getName())
                .setInterestRate(getInterest())
                .setDetails(buildLoanDetails(borrowers))
                .sourceInfo(buildSourceInfo())
                .build();
    }

    private LoanDetails buildLoanDetails(List<String> borrowers) {
        LoanDetails.Builder builder =
                LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                        .setNumMonthsBound(getNumMonthsBound())
                        .setNextDayOfTermsChange(getNextDayOfTermsChange())
                        .setSecurity(getSecurity())
                        .setApplicants(borrowers)
                        .setCoApplicant(borrowers.size() > 1);

        ExactCurrencyAmount monthlyAmortization = getMonthlyAmortization();

        if (monthlyAmortization != null) {
            builder.setMonthlyAmortization(monthlyAmortization);
        }

        return builder.build();
    }

    protected int getNumMonthsBound() {
        return loanDetails
                .map(LoanDetailsAccountEntity::getFixedInterestPeriod)
                .map(SwedbankSeSerializationUtils::parseNumMonthsBound)
                .orElse(0);
    }

    protected Date getNextDayOfTermsChange() {
        return loanDetails.map(LoanDetailsAccountEntity::getInterestFixedToDate).orElse(null);
    }

    private String getSecurity() {
        return loanDetails.map(LoanDetailsAccountEntity::getCollaterals)
                .orElseGet(Collections::emptyList).stream()
                .map(CollateralsEntity::getDescription)
                .collect(Collectors.joining(","));
    }
}
