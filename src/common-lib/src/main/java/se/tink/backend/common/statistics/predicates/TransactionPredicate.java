package se.tink.backend.common.statistics.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

/**
 * Default transaction predicate (predicates on account- and transaction
 * exclusions).
 */
public class TransactionPredicate implements Predicate<Transaction> {
    private final ImmutableSet<String> excludedAccountIds;
    private final String excludedCategoryId;

    public TransactionPredicate(List<Account> accounts, final List<Category> categories,
            final CategoryConfiguration categoryConfiguration) {

        excludedAccountIds = accounts.stream().filter(Account::isExcluded).map(Account::getId)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));

        excludedCategoryId = categories.stream()
                .filter(c -> (Objects.equal(categoryConfiguration.getExcludeCode(), c.getCode()))).findFirst().get()
                .getId();
    }

    @Override
    public boolean apply(Transaction t) {

        if (excludedAccountIds.contains(t.getAccountId())) {
            return false;
        }

        if (Objects.equal(excludedCategoryId, t.getCategoryId())) {
            return false;
        }

        return true;
    }
}
