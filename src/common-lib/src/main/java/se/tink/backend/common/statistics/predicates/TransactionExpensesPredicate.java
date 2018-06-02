package se.tink.backend.common.statistics.predicates;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.CategoryTypes;

public class TransactionExpensesPredicate extends TransactionCategoryTypePredicate {

    public TransactionExpensesPredicate() {
        super(ImmutableSet.of(CategoryTypes.EXPENSES));
    }
}
