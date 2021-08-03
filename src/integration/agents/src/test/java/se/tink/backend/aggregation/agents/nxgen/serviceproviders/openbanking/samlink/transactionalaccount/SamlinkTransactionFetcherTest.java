package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SamlinkTransactionFetcherTest {
    private BerlinGroupApiClient apiClient;
    private BerlinGroupTransactionFetcher fetcher;

    private static final TransactionsResponse TRANSACTIONS_PENDING_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"account\" : { \"iban\" : \"FI0000000000000003\" } , \"transactions\": { \"booked\" : [] , \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}, \"creditorName\" : \"JOHN DOE\" , \"remittanceInformationUnstructured\":\"DESCRIPTION\", \"debtorAccount\" : { \"iban\" : \"FI0000000000000003\" } } ] } }",
                    TransactionsResponse.class);

    private static final TransactionsResponse TRANSACTIONS_BOOKED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"account\" : { \"iban\" : \"FI0000000000000003\" } , \"transactions\": { \"booked\" : [{ \"entryReference\" : \"000000000000111\" , \"bookingDate\" : \"2000-10-10\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}, \"debtorName\" : \"JOHN DOE\" , \"remittanceInformationUnstructured\":\"DESCRIPTION\"} ] } }",
                    TransactionsResponse.class);

    @Before
    public void init() {
        apiClient = mock(SamlinkApiClient.class);
        fetcher = new BerlinGroupTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchPendingTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.fetchTransactions("URL")).thenReturn(TRANSACTIONS_PENDING_RESPONSE);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result = fetcher.getTransactionsFor(transactionalAccount, "URL");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("JOHN DOE");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
    }

    @Test
    public void shouldFetchBookedTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.fetchTransactions("URL")).thenReturn(TRANSACTIONS_BOOKED_RESPONSE);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result = fetcher.getTransactionsFor(transactionalAccount, "URL");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
    }
}
