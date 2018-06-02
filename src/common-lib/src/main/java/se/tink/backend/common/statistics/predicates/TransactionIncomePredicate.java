package se.tink.backend.common.statistics.predicates;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.CategoryTypes;

public class TransactionIncomePredicate extends TransactionCategoryTypePredicate {

    public TransactionIncomePredicate() {
        super(ImmutableSet.of(CategoryTypes.INCOME));
    }
}
