package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionPaginationController;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class StarlingTransactionPaginationControllerTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    @Mock
    private TransactionDatePaginationController<TransactionalAccount> defaultPaginationController;

    @Mock private TransactionDatePaginator<TransactionalAccount> paginator;
    @Mock private LocalDateTimeSource localDateTimeSource;
    private StarlingTransactionPaginationController<TransactionalAccount>
            transactionPaginationController;

    @Before
    public void setUp() throws Exception {
        this.transactionPaginationController =
                new StarlingTransactionPaginationController<>(
                        defaultPaginationController,
                        paginator,
                        localDateTimeSource,
                        DEFAULT_ZONE_ID);
    }

    @Test
    public void shouldUseDefaultTransactionPagination() {
        // given
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getFromTemporaryStorage(
                        StarlingConstants.ACCOUNT_CREATION_DATE_TIME, LocalDateTime.class))
                .thenReturn(Optional.empty());

        // when
        transactionPaginationController.fetchTransactionsFor(transactionalAccount);

        // then
        verify(defaultPaginationController, times(1)).fetchTransactionsFor(transactionalAccount);
    }

    @Test
    public void shouldReturnLastPagePageWhenAccountCreationDateIsAvailable() {
        // given
        LocalDateTime now = LocalDateTime.now();
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        PaginatorResponse paginatorResponse = mock(PaginatorResponse.class);
        when(transactionalAccount.getFromTemporaryStorage(
                        StarlingConstants.ACCOUNT_CREATION_DATE_TIME, LocalDateTime.class))
                .thenReturn(Optional.of(now));
        when(localDateTimeSource.now()).thenReturn(now);
        when(paginator.getTransactionsFor(
                        eq(transactionalAccount), any(Date.class), any(Date.class)))
                .thenReturn(paginatorResponse);
        when(paginatorResponse.getTinkTransactions()).thenReturn(Collections.emptyList());

        // when
        PaginatorResponse result =
                transactionPaginationController.fetchTransactionsFor(transactionalAccount);

        // then
        assertThat(result.canFetchMore().get()).isFalse();
    }

    @Test
    public void shouldReturnPageWithNextPageWhenAccountCreationDateIsAvailable() {
        // given
        LocalDateTime now = LocalDateTime.now();
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        PaginatorResponse paginatorResponse = mock(PaginatorResponse.class);
        when(transactionalAccount.getFromTemporaryStorage(
                        StarlingConstants.ACCOUNT_CREATION_DATE_TIME, LocalDateTime.class))
                .thenReturn(Optional.of(now.minusMonths(5)));
        when(localDateTimeSource.now()).thenReturn(now);
        when(paginator.getTransactionsFor(
                        eq(transactionalAccount), any(Date.class), any(Date.class)))
                .thenReturn(paginatorResponse);
        when(paginatorResponse.getTinkTransactions()).thenReturn(Collections.emptyList());

        // when
        PaginatorResponse result =
                transactionPaginationController.fetchTransactionsFor(transactionalAccount);

        // then
        assertThat(result.canFetchMore().get()).isTrue();
    }
}
