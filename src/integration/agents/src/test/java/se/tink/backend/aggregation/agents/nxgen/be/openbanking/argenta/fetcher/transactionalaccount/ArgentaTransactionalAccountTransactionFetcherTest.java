package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class ArgentaTransactionalAccountTransactionFetcherTest {
    @Mock ArgentaApiClient apiClient;
    @InjectMocks ArgentaTransactionalAccountTransactionFetcher fetcher;

    private URL url;

    private TransactionKeyPaginatorResponse<String> setUrl(URL buildedUrl) {
        url = buildedUrl;
        return null;
    }

    @Test
    public void testTransactionKeyUrlBuilder() {

        // given
        final String date = "2021-07-26";
        final String fromDate = "2021-04-28";
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);

        ArgentaTransactionalAccountTransactionFetcher
                argentaTransactionalAccountTransactionFetcher =
                        new ArgentaTransactionalAccountTransactionFetcher(apiClient);

        // when
        when(transactionalAccount.getFromTemporaryStorage(any())).thenReturn("/DumyMockData");
        when(apiClient.getTransactions(any())).thenAnswer(i -> setUrl((URL) i.getArguments()[0]));
        when(apiClient.getDate()).thenReturn(LocalDate.parse(date));

        argentaTransactionalAccountTransactionFetcher.getTransactionsFor(
                transactionalAccount, null);

        // then
        List<NameValuePair> params = URLEncodedUtils.parse(url.toUri(), Charset.forName("UTF-8"));
        assertThat(params)
                .contains(
                        (NameValuePair)
                                new BasicNameValuePair(
                                        ArgentaConstants.QueryKeys.DATE_FROM, fromDate));
    }

    @Test
    public void shouldFetchTransactionsAndConvertItToTinkModel() {
        // given
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);

        when(apiClient.getTransactions(any()))
                .thenReturn(ArgentaFetcherTestData.TRANSACTIONS_RESPONSE);

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
