package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngBaseTransactionsFetcherTest {

    private IngBaseApiClient apiClient = mock(IngBaseApiClient.class);

    private static final BaseFetchTransactionsResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\" : {} , \"transactions\" : { \"booked\" : [{ \"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"} }], \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}}]} }",
                    BaseFetchTransactionsResponse.class);

    private IngBaseTransactionsFetcher fetcher =
            new IngBaseTransactionsFetcher(apiClient, LocalDate::now);

    @Test
    public void shouldFetchAndMapTransactionsToTinkModel() {
        // given
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        given(apiClient.fetchTransactionsPage(anyString())).willReturn(TRANSACTIONS_RESPONSE);

        // when
        TransactionKeyPaginatorResponse<String> result =
                fetcher.getTransactionsFor(transactionalAccount, "key");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
    }
}
