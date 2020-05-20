package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ArgentaTransactionalAccountTransactionFetcherTest {
    private ArgentaApiClient apiClient;
    private ArgentaTransactionalAccountTransactionFetcher fetcher;

    private static final TransactionsResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\" : {} , \"transactions\" : { \"booked\" : [{ \"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"} }], \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}}]} }",
                    TransactionsResponse.class);

    @Before
    public void init() {
        apiClient = mock(ArgentaApiClient.class);
        fetcher = new ArgentaTransactionalAccountTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionsAndConvertItToTinkModel() {
        // given
        Date now = new Date();
        when(apiClient.getTransactions("API_IDENTIFIER", now, now))
                .thenReturn(TRANSACTIONS_RESPONSE);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result = fetcher.getTransactionsFor(transactionalAccount, now, now);

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
        verify(apiClient).getTransactions("API_IDENTIFIER", now, now);
        verifyNoMoreInteractions(apiClient);
    }
}
