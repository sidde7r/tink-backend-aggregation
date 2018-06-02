package se.tink.backend.common.statistics.functions;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.List;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

/**
 * Default transaction predicate (predicates on account- and transaction
 * exclusions).
 */
public class TransactionPredicate implements Predicate<Transaction> {
    protected final ImmutableSet<String> excludedAccountIds;
    protected final String excludedCategoryId;
    protected final ImmutableMap<String, Category> categoriesById;

    public TransactionPredicate(List<Account> accounts, final List<Category> categories,
            final CategoryConfiguration categoryConfiguration) {
        excludedAccountIds = ImmutableSet.copyOf(Iterables.transform(
                Iterables.filter(accounts, Account::isExcluded), Account::getId));

        excludedCategoryId = Iterables.find(categories,
                c -> (Objects.equal(categoryConfiguration.getExcludeCode(), c.getCode()))).getId();

        categoriesById = Maps.uniqueIndex(categories, Category::getId);
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
