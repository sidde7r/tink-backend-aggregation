package se.tink.backend.common.dao.transactions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.libraries.metrics.MetricRegistry;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionDaoTest {
    @InjectMocks TransactionDao dao;
    @Mock TransactionRepository transactionRepository;
    @Mock TransactionSearchIndex transactionSearchIndex;
    @Mock CategoryRepository categoryRepository;
    @Mock MetricRegistry registry;

    @Test
    public void indexEmptyTransactionList() {
        boolean async = false;
        //noinspection ConstantConditions
        dao.index(Collections.<Transaction>emptyList(), async);
        verifyNoMoreInteractions(transactionSearchIndex);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void indexTransactions() {
        boolean async = false;
        List<Transaction> transactions = asList(new Transaction(), new Transaction());
        dao.index(transactions, async);

        //noinspection unchecked
        verify(transactionSearchIndex).index(eq(transactions), eq(async), any(Function.class));
    }

    @Test
    public void categoryIdToCategory() {
        Category category = new Category();
        when(categoryRepository.getDefaultLocale()).thenReturn("locale");
        when(categoryRepository.getCategoriesById("locale")).thenReturn(ImmutableMap.of("categoryId", category));

        assertEquals(Optional.of(category), dao.categoryIdToCategory().apply("categoryId"));
    }

    @Test
    public void categoryIdToCategoryWhenNoCategory() {
        when(categoryRepository.getDefaultLocale()).thenReturn("locale");
        when(categoryRepository.getCategoriesById("locale")).thenReturn(Collections.<String, Category>emptyMap());

        assertEquals(Optional.empty(), dao.categoryIdToCategory().apply("categoryId"));
    }

    @Test
    public void categoryIdToCategoryWhenNoCategoryId() {
        assertEquals(Optional.empty(), dao.categoryIdToCategory().apply(null));
    }

    @Test
    public void deleteTransactionsByEmptyAccountIds() {
        dao.deleteByAccountIds(Collections.<String>emptyList());

        verifyNoMoreInteractions(transactionSearchIndex);
        verifyNoMoreInteractions(transactionRepository);
    }

    private Account newAccount(String accountId, String userId) {
        Account account = new Account();
        account.setId(accountId);
        account.setUserId(userId);
        return account;
    }

}
