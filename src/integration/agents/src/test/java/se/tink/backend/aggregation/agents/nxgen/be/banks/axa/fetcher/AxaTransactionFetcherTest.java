package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class AxaTransactionFetcherTest {

    @Mock private AxaStorage axaStorage;

    @Mock private AxaApiClient axaApiClient;

    @InjectMocks private AxaTransactionFetcher axaTransactionFetcher;

    @Test
    public void shouldChangeUnallowedLocale() {
        when(axaStorage.getCustomerId()).thenReturn(Optional.of(1234));
        when(axaStorage.getAccessToken()).thenReturn(Optional.of("ACCESS TOKEN"));
        when(axaStorage.getLanguage()).thenReturn(Optional.of("en"));

        when(axaApiClient.postGetTransactions(anyInt(), any(), any(), any()))
                .thenReturn(new GetTransactionsResponse());

        TransactionalAccount account = mockAccount();

        axaTransactionFetcher.fetchTransactionsFor(account);

        verify(axaApiClient).postGetTransactions(anyInt(), any(), any(), eq("nl"));
    }

    @Test
    public void shouldKeepAllowedLocale() {
        when(axaStorage.getCustomerId()).thenReturn(Optional.of(1234));
        when(axaStorage.getAccessToken()).thenReturn(Optional.of("ACCESS TOKEN"));
        when(axaStorage.getLanguage()).thenReturn(Optional.of("sv"));

        when(axaApiClient.postGetTransactions(anyInt(), any(), any(), any()))
                .thenReturn(new GetTransactionsResponse());

        TransactionalAccount account = mockAccount();

        axaTransactionFetcher.fetchTransactionsFor(account);

        verify(axaApiClient).postGetTransactions(anyInt(), any(), any(), eq("sv"));
    }

    private TransactionalAccount mockAccount() {
        return TransactionalAccount.builder(
                        AccountTypes.CHECKING, "UQ1234", ExactCurrencyAmount.inEUR(123.45))
                .setBankIdentifier("BI1234")
                .setAccountNumber("AC1234")
                .build();
    }
}
