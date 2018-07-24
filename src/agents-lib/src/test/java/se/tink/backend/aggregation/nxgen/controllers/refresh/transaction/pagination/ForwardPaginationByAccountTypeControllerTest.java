package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.TestAccountBuilder;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class ForwardPaginationByAccountTypeControllerTest {
    private ForwardPaginationByAccountTypeController paginator;
    private final TransactionPagePaginationController defaultPaginator = Mockito.mock(TransactionPagePaginationController.class);
    private final TransactionDatePaginationController datePaginator = Mockito.mock(TransactionDatePaginationController.class);
    private final TransactionMonthPaginationController monthPaginator = Mockito.mock(TransactionMonthPaginationController.class);

    private final ImmutableMap<AccountTypes, TransactionPaginator> paginatorsByAccountType = ImmutableMap.<AccountTypes, TransactionPaginator>builder()
            .put(AccountTypes.CREDIT_CARD, datePaginator)
            .put(AccountTypes.SAVINGS, monthPaginator)
            .build();

    private InOrder executionOrder;

    @Before
    public void setup() {
        paginator = new ForwardPaginationByAccountTypeController(defaultPaginator, paginatorsByAccountType);
        executionOrder = Mockito.inOrder(defaultPaginator, datePaginator, monthPaginator);
    }

    @After
    public void after() {
        executionOrder.verifyNoMoreInteractions();
    }

    /**
     * Instantiation tests
     */
    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenDefaultPaginator_isNull() {
        new ForwardPaginationByAccountTypeController(null, paginatorsByAccountType);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenPaginatorsByAccountType_isNull() {
        new ForwardPaginationByAccountTypeController(defaultPaginator, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionIsThrown_whenPaginatorsByAccountType_isEmpty() {
        new ForwardPaginationByAccountTypeController(defaultPaginator, Collections.emptyMap());
    }

    /**
     * Forwarding tests
     */
    @Test
    public void ensureDefaultPaginatorIsUsed_whenPaginatorsByAccountType_doesNotContainPaginatorOfAccountType() {
        CheckingAccount account = TestAccountBuilder.from(CheckingAccount.class).build();
        paginator.fetchTransactionsFor(account);
        paginator.canFetchMoreFor(account);

        final TransactionPaginator creditCardPaginator = paginatorsByAccountType.get(AccountTypes.CREDIT_CARD);
        final TransactionPaginator savingsPaginator = paginatorsByAccountType.get(AccountTypes.SAVINGS);

        executionOrder.verify(creditCardPaginator, Mockito.never()).fetchTransactionsFor(account);
        executionOrder.verify(creditCardPaginator, Mockito.never()).canFetchMoreFor(account);
        executionOrder.verify(savingsPaginator, Mockito.never()).fetchTransactionsFor(account);
        executionOrder.verify(savingsPaginator, Mockito.never()).canFetchMoreFor(account);

        executionOrder.verify(defaultPaginator).fetchTransactionsFor(account);
        executionOrder.verify(defaultPaginator).canFetchMoreFor(account);
    }

    @Test
    public void ensureCorrectPaginatorIsUsed_whenPaginatorsByAccountType_containsPaginatorOfAccountType() {
        CreditCardAccount account = TestAccountBuilder.from(CreditCardAccount.class).build();
        paginator.fetchTransactionsFor(account);
        paginator.canFetchMoreFor(account);

        final TransactionPaginator creditCardPaginator = paginatorsByAccountType.get(AccountTypes.CREDIT_CARD);
        final TransactionPaginator savingsPaginator = paginatorsByAccountType.get(AccountTypes.SAVINGS);

        executionOrder.verify(defaultPaginator, Mockito.never()).fetchTransactionsFor(account);
        executionOrder.verify(defaultPaginator, Mockito.never()).canFetchMoreFor(account);
        executionOrder.verify(savingsPaginator, Mockito.never()).fetchTransactionsFor(account);
        executionOrder.verify(savingsPaginator, Mockito.never()).canFetchMoreFor(account);
        executionOrder.verify(creditCardPaginator).fetchTransactionsFor(account);
        executionOrder.verify(creditCardPaginator).canFetchMoreFor(account);
    }

    @Test
    public void ensureMultiplePaginators_isAllowed() {
        final TransactionPaginator creditCardPaginator = paginatorsByAccountType.get(AccountTypes.CREDIT_CARD);
        final TransactionPaginator savingsPaginator = paginatorsByAccountType.get(AccountTypes.SAVINGS);

        Account account = TestAccountBuilder.from(CheckingAccount.class).build();
        paginator.fetchTransactionsFor(account);
        paginator.canFetchMoreFor(account);
        executionOrder.verify(defaultPaginator).fetchTransactionsFor(account);
        executionOrder.verify(defaultPaginator).canFetchMoreFor(account);

        account = TestAccountBuilder.from(CreditCardAccount.class).build();
        paginator.fetchTransactionsFor(account);
        paginator.canFetchMoreFor(account);
        executionOrder.verify(creditCardPaginator).fetchTransactionsFor(account);
        executionOrder.verify(creditCardPaginator).canFetchMoreFor(account);

        account = TestAccountBuilder.from(SavingsAccount.class).build();
        paginator.fetchTransactionsFor(account);
        paginator.canFetchMoreFor(account);
        executionOrder.verify(savingsPaginator).fetchTransactionsFor(account);
        executionOrder.verify(savingsPaginator).canFetchMoreFor(account);
    }
}
