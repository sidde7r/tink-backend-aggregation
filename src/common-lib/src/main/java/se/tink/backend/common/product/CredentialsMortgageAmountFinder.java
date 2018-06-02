package se.tink.backend.common.product;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.utils.MortgageCalculator;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.MortgageMeasure;
import se.tink.backend.core.User;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

public class CredentialsMortgageAmountFinder {
    private final MortgageCalculator mortgageCalculator;

    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;

    @Inject
    public CredentialsMortgageAmountFinder(
            MortgageCalculator mortgageCalculator,
            AccountRepository accountRepository,
            CredentialsRepository credentialsRepository) {
        this.mortgageCalculator = mortgageCalculator;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
    }

    public Optional<MortgageMeasure> getMortgageMeasure(User user) {
        ListMultimap<String, Account> accountsByCredentialsId = FluentIterable
                .from(accountRepository.findByUserId(user.getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_NOT_CLOSED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .index(Account::getCredentialsId);

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

        Optional<MortgageMeasure> mortgageMeasure = Optional.empty();

        for (Credentials c : credentials) {
            List<Account> accounts = accountsByCredentialsId.get(c.getId());

            if (accounts.isEmpty()) {
                continue;
            }

            mortgageMeasure = chooseCandidate(mortgageCalculator.aggregate(accounts), mortgageMeasure);
        }

        return mortgageMeasure;
    }

    private Optional<MortgageMeasure> chooseCandidate(MortgageMeasure challenger, Optional<MortgageMeasure> current) {
        // Choose the challenger if the interest rate cost is higher than the current candidate.
        if (!current.isPresent() || current.get().getCost() < challenger.getCost()) {
            return Optional.of(challenger);
        }

        return current;
    }

    public double getMortgageAmount(User user) {
        return getMortgageMeasure(user).map(MortgageMeasure::getAmount).orElse(0d);
    }
}
