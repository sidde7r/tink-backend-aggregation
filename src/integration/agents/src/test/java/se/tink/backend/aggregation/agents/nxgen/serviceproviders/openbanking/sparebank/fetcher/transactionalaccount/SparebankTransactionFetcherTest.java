package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SparebankTransactionFetcherTest {

    private static SparebankApiClient apiClient;
    private static TransactionalAccount account;
    private static HttpResponse httpResponse;

    private static SparebankTransactionFetcher transactionFetcher;
    private static Date anyDate = new Date();

    @BeforeClass
    public static void setup() {
        apiClient = mock(SparebankApiClient.class);
        account = mock(TransactionalAccount.class);
        when(account.getApiIdentifier()).thenReturn("doesNotMatter");
        httpResponse = mock(HttpResponse.class);

        transactionFetcher = new SparebankTransactionFetcher(apiClient);
    }

    @Test
    public void shouldConsumeExceptionAndReturnEmptyResponseInCaseOfScaRedirectException() {
        when(httpResponse.getBody(any())).thenReturn("SomethingSomethingscaRedirect12445zxcvasdf");
        when(apiClient.fetchTransactions(anyString()))
                .thenThrow(new HttpResponseException("", null, httpResponse));

        PaginatorResponse response = transactionFetcher.getTransactionsFor(account, null);

        assertEquals(0, response.getTinkTransactions().size());
        assertEquals(false, response.canFetchMore().get());
    }

    @Test
    public void shouldRethrowExceptionWhenExceptionDoesNotMentionScaRedirect() {
        when(httpResponse.getBody(any())).thenReturn("123456");
        String message = "A example message to test rethrow";
        Exception exception = new HttpResponseException(message, null, httpResponse);
        when(apiClient.fetchTransactions(anyString())).thenThrow(exception);

        Throwable throwable =
                catchThrowable(() -> transactionFetcher.getTransactionsFor(account, null));

        assertThat(throwable).isEqualTo(exception);
    }
}
