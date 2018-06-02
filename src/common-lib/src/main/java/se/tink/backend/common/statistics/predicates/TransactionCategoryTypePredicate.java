package se.tink.backend.common.statistics.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;

public abstract class TransactionCategoryTypePredicate implements Predicate<Transaction> {

    protected final ImmutableSet<CategoryTypes> categoryTypes;

    TransactionCategoryTypePredicate(ImmutableSet<CategoryTypes> categoryTypes) {
        this.categoryTypes = categoryTypes;
    }

    @Override
    public boolean apply(Transaction t) {
        // Note that this implementation only look at the category type from the actual transaction, category types on
        // individual parts are ignored since it isn't needed in phase 1. Adding support for this adds complexity in
        // the current statistics implementation.
        return categoryTypes.contains(t.getCategoryType());
    }
}
