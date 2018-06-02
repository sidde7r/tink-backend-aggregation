package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Loan;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

public class MortgagePredicate implements Predicate<Profile> {
    private final AccountRepository accountRepository;
    private final LoanDataRepository loanDataRepository;

    private final String criteria;
    private final String provider;

    @Inject
    public MortgagePredicate(ProductFilterRule rule, AccountRepository accountRepository,
            LoanDataRepository loanDataRepository) {
        this.accountRepository = accountRepository;
        this.loanDataRepository = loanDataRepository;

        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) rule.getPayload();
        this.criteria = payload.get("criteria");
        this.provider = payload.get("provider");
    }

    @Override
    public boolean apply(Profile profile) {
        boolean invert = Objects.equals(criteria, "exclude");
        return hasAccount(profile) ^ invert;
    }

    private boolean hasAccount(Profile profile) {

        Collection<Credentials> credentials;

        if (Objects.equals("*", provider)) {
            credentials = profile.getCredentials().values();
        } else {
            credentials = profile.getCredentials().get(provider);
        }

        if (credentials == null || credentials.isEmpty()) {
            return false;
        }

        Set<String> credentialsIds = FluentIterable.from(credentials).transform(Credentials::getId).toSet();

        Iterable<Account> accounts = FluentIterable.from(accountRepository.findByUserId(profile.getUser().getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE));

        for (Account a : accounts) {
            if (!credentialsIds.contains(a.getCredentialsId())) {
                continue;
            }

            Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());
            if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("criteria", criteria)
                .add("provider", provider)
                .toString();
    }
}
