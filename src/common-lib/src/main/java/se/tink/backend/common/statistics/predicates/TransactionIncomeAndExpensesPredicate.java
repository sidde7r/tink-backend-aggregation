package se.tink.backend.common.statistics.predicates;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.CategoryTypes;

public class TransactionIncomeAndExpensesPredicate extends TransactionCategoryTypePredicate {

    public TransactionIncomeAndExpensesPredicate() {
        super(ImmutableSet.of(CategoryTypes.INCOME, CategoryTypes.EXPENSES));
    }
}
