package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.AccountConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity.CategoryType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc.TransactionResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RunWith(MockitoJUnitRunner.class)
public class MetroTransactionFetcherTest {
    private static final String CURRENCY = "GBP";

    private static final String ACCOUNT_ID = "111111";

    @Mock private TransactionClient transactionClient;

    @Mock private TransactionMapper mapper;

    private MetroTransactionFetcher metroTransactionFetcher;

    @Before
    public void setUp() throws Exception {
        this.metroTransactionFetcher = new MetroTransactionFetcher(transactionClient, mapper);
    }

    @Test
    public void shouldFetchTransactionsForGivenAccount() {
        // given
        TransactionalAccount account = getTransactionalAccount();
        TransactionResponse transactionResponse = mock(TransactionResponse.class);
        when(transactionResponse.getTransactions())
                .thenReturn(Collections.singletonList(mock(TransactionEntity.class)));
        when(transactionClient.fetchTransactions(any()))
                .thenReturn(Either.right(transactionResponse));
        when(mapper.map(any(), eq(CURRENCY))).thenReturn(mock(AggregationTransaction.class));

        // when
        List<AggregationTransaction> transactions =
                metroTransactionFetcher.fetchTransactionsFor(account);

        // then
        assertThat(transactions.isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNotTransactionsForGivenAccount() {
        // given
        TransactionalAccount account = getTransactionalAccount();
        when(transactionClient.fetchTransactions(any()))
                .thenReturn(Either.left(new AuthorizationError()));

        // when
        List<AggregationTransaction> transactions =
                metroTransactionFetcher.fetchTransactionsFor(account);

        // then
        assertThat(transactions.isEmpty()).isTrue();
    }

    private TransactionalAccount getTransactionalAccount() {
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getFromTemporaryStorage(AccountConstants.CURRENCY)).thenReturn(CURRENCY);
        when(account.getFromTemporaryStorage(AccountConstants.ACCOUNT_TYPE))
                .thenReturn(CategoryType.CURRENT_ACCOUNT.name());
        when(account.getFromTemporaryStorage(AccountConstants.ACCOUNT_ID)).thenReturn(ACCOUNT_ID);
        when(account.getFromTemporaryStorage(AccountConstants.CREATION_DATE))
                .thenReturn(LocalDate.now().toString());
        return account;
    }
}
