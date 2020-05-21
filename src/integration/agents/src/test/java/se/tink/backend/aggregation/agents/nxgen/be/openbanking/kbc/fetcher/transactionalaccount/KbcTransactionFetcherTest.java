package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcTransactionFetcherTest {
    private BerlinGroupApiClient apiClient;
    private BerlinGroupTransactionFetcher fetcher;

    private static final TransactionsKeyPaginatorBaseResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\" : {} , \"transactions\" : { \"booked\" : [{ \"bookingDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"} }], \"pending\" : [{\"bookingDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}}]} }",
                    TransactionsKeyPaginatorBaseResponse.class);

    @Before
    public void init() {
        apiClient = mock(KbcApiClient.class);
        fetcher = new BerlinGroupTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.fetchTransactions("URL")).thenReturn(TRANSACTIONS_RESPONSE);
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
