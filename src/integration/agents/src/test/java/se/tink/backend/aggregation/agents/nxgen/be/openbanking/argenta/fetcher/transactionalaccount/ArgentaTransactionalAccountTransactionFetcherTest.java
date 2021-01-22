package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ArgentaTransactionalAccountTransactionFetcherTest {
    private ArgentaApiClient apiClient;
    private ArgentaTransactionalAccountTransactionFetcher fetcher;

    @Before
    public void init() {
        apiClient = mock(ArgentaApiClient.class);
        fetcher = new ArgentaTransactionalAccountTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.getTransactions(any()))
                .thenReturn(ArgentaFetcherTestData.TRANSACTIONS_RESPONSE);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result =
                fetcher.getTransactionsFor(transactionalAccount, "API_IDENTIFIER");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
        verify(apiClient).getTransactions(any());
        verifyNoMoreInteractions(apiClient);
    }
}
