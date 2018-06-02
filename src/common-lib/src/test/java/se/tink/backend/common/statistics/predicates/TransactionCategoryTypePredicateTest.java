package se.tink.backend.common.statistics.predicates;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionCategoryTypePredicateTest {

    @Test
    public void testMatchedTransaction(){
        Category expenseCategory = new Category();
        expenseCategory.setId(StringUtils.generateUUID());
        expenseCategory.setType(CategoryTypes.EXPENSES);

        Transaction transaction = new Transaction();
        transaction.setCategory(expenseCategory);

        assertThat(new TransactionCategoryTypePredicate(ImmutableSet.of(CategoryTypes.EXPENSES)) {}.apply(transaction)).isTrue();
    }

    @Test
    public void testNotMatchedTransaction(){
        Category expenseCategory = new Category();
        expenseCategory.setId(StringUtils.generateUUID());
        expenseCategory.setType(CategoryTypes.EXPENSES);

        Transaction transaction = new Transaction();
        transaction.setCategory(expenseCategory);

        assertThat(new TransactionCategoryTypePredicate(ImmutableSet.of(CategoryTypes.INCOME)) {}.apply(transaction)).isFalse();
    }
}
