package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SamlinkTransactionFetcherTest {
    private BerlinGroupApiClient apiClient;
    private BerlinGroupTransactionFetcher fetcher;
    private TransactionalAccount transactionalAccount;

    private static final TransactionsResponse TRANSACTIONS_PENDING_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"account\" : { \"iban\" : \"FI0000000000000003\" } , \"transactions\": { \"booked\" : [] , \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}, \"creditorName\" : \"JOHN DOE\" , \"remittanceInformationUnstructured\":\"DESCRIPTION\", \"debtorAccount\" : { \"iban\" : \"FI0000000000000003\" } } ] } }",
                    TransactionsResponse.class);

    private static final TransactionsResponse TRANSACTIONS_BOOKED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"account\" : { \"iban\" : \"FI0000000000000003\" } , \"transactions\": { \"booked\" : [{ \"entryReference\" : \"000000000000111\" , \"bookingDate\" : \"2000-10-10\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"},  \"remittanceInformationUnstructured\":\"DESCRIPTION\"} ] } }",
                    TransactionsResponse.class);

    @Before
    public void init() {
        transactionalAccount = mock(TransactionalAccount.class);
        apiClient = mock(SamlinkApiClient.class);
        fetcher = new BerlinGroupTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchPendingTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.fetchTransactions("URL")).thenReturn(TRANSACTIONS_PENDING_RESPONSE);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result = fetcher.getTransactionsFor(transactionalAccount, "URL");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("JOHN DOE");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(-6.66));
    }

    @Test
    public void shouldFetchBookedTransactionsAndConvertItToTinkModel() {
        // given
        when(apiClient.fetchTransactions("URL")).thenReturn(TRANSACTIONS_BOOKED_RESPONSE);
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        // when
        PaginatorResponse result = fetcher.getTransactionsFor(transactionalAccount, "URL");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
        assertThat(transaction.getTransactionReference()).isEqualTo("000000000000111");
    }

    @Test
    public void shouldReturnEmptyTransactionKeyPaginatorResponseWhenServiceExceptionOccurs() {
        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"category\": \"ERROR\",\n"
                        + "\t\t\t\"code\": \"SERVICE_INVALID\",\n"
                        + "\t\t\t\"text\": \"Account has no payment privileges\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        SamlinkAgentsConfiguration samlinkAgentsConfiguration =
                mock(SamlinkAgentsConfiguration.class);
        when(samlinkAgentsConfiguration.getBaseUrl()).thenReturn("URL");
        when(transactionalAccount.getApiIdentifier()).thenReturn("API_IDENTIFIER");

        HttpResponse httpResponse = mockResponse(responseBody);
        SamlinkTransactionFetcher samlinkTransactionFetcher =
                new SamlinkTransactionFetcher(apiClient, samlinkAgentsConfiguration);
        when(apiClient.fetchTransactions(any()))
                .thenThrow(new HttpResponseException(null, httpResponse));

        // when
        TransactionKeyPaginatorResponse<String> transactions =
                samlinkTransactionFetcher.getTransactionsFor(transactionalAccount, "URL");

        // then
        assertThat(transactions.getTinkTransactions()).isEqualTo(Collections.emptyList());
    }

    private HttpResponse mockResponse(String responseBody) {
        ErrorResponse errorResponse = new Gson().fromJson(responseBody, ErrorResponse.class);
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        return mocked;
    }
}
