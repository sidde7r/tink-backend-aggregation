package se.tink.backend.common.statistics.predicates;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Objects;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

public class TransactionSavingsPredicate implements Predicate<Transaction> {
    private final String savingsCategoryId;

    public TransactionSavingsPredicate(List<Category> categories, final CategoryConfiguration categoryConfiguration) {
        savingsCategoryId = categories.stream()
                .filter(category -> (Objects.equals(category.getCode(), categoryConfiguration.getSavingsCode())))
                .findFirst().get().getId();
    }

    @Override
    public boolean apply(Transaction t) {
        return (Objects.equals(t.getCategoryId(), savingsCategoryId) && t.getAmount() < 0);
    }
}
