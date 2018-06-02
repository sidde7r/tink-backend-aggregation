package se.tink.backend.common.utils;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Loan;
import se.tink.backend.core.MortgageMeasure;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

public class MortgageCalculator {
    private final LoanDataRepository loanDataRepository;

    @Inject
    public MortgageCalculator(LoanDataRepository loanDataRepository) {
        this.loanDataRepository = loanDataRepository;
    }

    public MortgageMeasure aggregate(List<Account> accounts) {
        List<Account> mortgageAccounts = filterAccounts(accounts);

        if (accounts.isEmpty()) {
            return new MortgageMeasure();
        }

        double mortgageAmount = 0;
        double mortgageCost = 0;

        for (Account account : mortgageAccounts) {
            Loan loan = loanDataRepository.findMostRecentOneByAccountId(account.getId());

            if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE) && loan.getInterest() != null
                    && loan.getBalance() != null) {
                mortgageAmount += Math.abs(loan.getBalance());
                mortgageCost += Math.abs(loan.getBalance()) * loan.getInterest();
            }
        }

        MortgageMeasure mortgageMeasure = new MortgageMeasure();
        mortgageMeasure.setAmount(mortgageAmount);
        mortgageMeasure.setCost(mortgageCost);
        if (!Doubles.fuzzyEquals(mortgageAmount, 0D, 0.01)) {
            mortgageMeasure.setRate(mortgageCost / mortgageAmount);
        }

        return mortgageMeasure;
    }

    private List<Account> filterAccounts(List<Account> accounts) {
        return accounts.stream()
                .filter(AccountPredicate.IS_NOT_EXCLUDED::apply)
                .filter(AccountPredicate.IS_NOT_CLOSED::apply)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE)::apply)
                .collect(Collectors.toList());
    }
}
