package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.Loan;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

public class MortgageAmountPredicate implements Predicate<Profile> {
    private final AccountRepository accountRepository;
    private final LoanDataRepository loanDataRepository;

    private final int min;
    private final int max;

    public MortgageAmountPredicate(ProductFilterRule rule,
            AccountRepository accountRepository,
            LoanDataRepository loanDataRepository) {
        this.accountRepository = accountRepository;
        this.loanDataRepository = loanDataRepository;

        @SuppressWarnings("unchecked")
        Map<String, Integer> payload = (Map<String, Integer>) rule.getPayload();

        this.min = payload.containsKey("min") ? payload.get("min") : 0;
        this.max = payload.containsKey("max") ? payload.get("max") : Integer.MAX_VALUE;
    }

    @Override
    public boolean apply(Profile profile) {

        List<Account> accounts = accountRepository.findByUserId(profile.getUser().getId());

        if (accounts == null || accounts.isEmpty()) {
            return false;
        }

        ImmutableListMultimap<String, Account> accountsByCredentialsId = FluentIterable.from(accounts)
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_NOT_CLOSED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .index(Account::getCredentialsId);

        String ssn = profile.getUser().getProfile().getFraudPersonNumber();

        for (Credentials credentials : profile.getCredentials().values()) {
            // Ignore credentials that have a different username than the user's SSN.
            if (!Objects.equals(ssn, credentials.getField(Field.Key.USERNAME))) {
                continue;
            }

            if (DemoCredentials.isDemoUser(credentials.getField(Field.Key.USERNAME))) {
                continue;
            }

            double loanAmount = 0;

            for (Account a : accountsByCredentialsId.get(credentials.getId())) {

                Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());

                if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE) && loan.getBalance() != null) {
                    loanAmount += Math.abs(loan.getBalance());
                }
            }

            if (loanAmount >= min && loanAmount <= max) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}
