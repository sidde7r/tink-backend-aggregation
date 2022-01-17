package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaFetcherFixtures.TRANSACTION_RESPONSE_WITH_UNSTRUCTURED_REMITTANCE_INFORMATION;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ArkeaTransactionFetcherTest {

    private final String nextPagePath = "/psd2/v1/accounts/Alias1/transactions?page=1";
    private final String apiIdentifier = "apiIdentifier";

    @Mock private TransactionalAccount account;
    @Mock private ArkeaApiClient apiClient;

    @InjectMocks private ArkeaTransactionFetcher<TransactionalAccount> transactionFetcher;

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        when(account.getApiIdentifier()).thenReturn(apiIdentifier);
    }

    @Test
    @Parameters({"/psd2/v1/accounts/Alias1/transactions?page=1", "null"})
    public void shouldFetchTransactions(@Nullable String nextPageURL) {
        // given
        when(apiClient.getTransactions(eq(apiIdentifier), any()))
                .thenReturn(TRANSACTION_RESPONSE_WITH_UNSTRUCTURED_REMITTANCE_INFORMATION);

        // when
        TransactionKeyPaginatorResponse<String> transactionResponse =
                transactionFetcher.getTransactionsFor(account, nextPageURL);

        // then
        assertThat(transactionResponse)
                .isEqualTo(TRANSACTION_RESPONSE_WITH_UNSTRUCTURED_REMITTANCE_INFORMATION);

        // and
        verify(apiClient).getTransactions(apiIdentifier, nextPageURL);
    }

    @Test(expected = HttpResponseException.class)
    public void shouldRethrowExceptionAfterOtherThanNoContentResponse() {
        // given
        HttpResponseException exception = mock(HttpResponseException.class);
        HttpResponse response = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(response);
        when(response.getStatus()).thenReturn(401);

        when(apiClient.getTransactions(apiIdentifier, nextPagePath)).thenThrow(exception);

        // when
        transactionFetcher.getTransactionsFor(account, nextPagePath);
    }

    @Test
    public void shouldReturnEmptyPaginatorResponseWhenStatusIsNoContent() {
        // given
        HttpResponseException exception = mock(HttpResponseException.class);
        HttpResponse response = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        when(apiClient.getTransactions(apiIdentifier, nextPagePath)).thenThrow(exception);

        // when
        TransactionKeyPaginatorResponse<String> transactionResponse =
                transactionFetcher.getTransactionsFor(account, nextPagePath);

        // then
        assertThat(TransactionKeyPaginatorResponseImpl.createEmpty())
                .usingRecursiveComparison()
                .isEqualTo(transactionResponse);
    }
}
