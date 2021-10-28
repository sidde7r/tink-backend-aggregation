package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AuthSessionStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountsOverviewResponse;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AvanzaLoanFetcherTest {

    private AvanzaLoanFetcher loanFetcher;
    private AvanzaApiClient apiClient;

    @Before
    public void setup() {
        this.apiClient = mock(AvanzaApiClient.class);
        this.loanFetcher =
                new AvanzaLoanFetcher(
                        apiClient,
                        mock(AuthSessionStorageHelper.class),
                        mock(TemporaryStorage.class));
    }

    @Test
    public void shouldReturnEmptyListWhenBankSendsEmptyAccountResponse() {
        when(apiClient.fetchAccounts(Mockito.any())).thenReturn(getEmptyAccountsOverviewResponse());
        assertEquals(Collections.emptyList(), loanFetcher.fetchAccounts());
    }

    private AccountsOverviewResponse getEmptyAccountsOverviewResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\": []}", AccountsOverviewResponse.class);
    }
}
