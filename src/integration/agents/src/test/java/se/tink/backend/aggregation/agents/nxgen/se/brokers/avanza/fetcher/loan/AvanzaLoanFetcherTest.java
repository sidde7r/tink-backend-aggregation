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

    AvanzaLoanFetcher loanFetcher;
    AvanzaApiClient apiClient;
    AuthSessionStorageHelper authSessionStorage;
    TemporaryStorage temporaryStorage;

    @Before
    public void setup() {
        this.temporaryStorage = mock(TemporaryStorage.class);
        this.authSessionStorage = mock(AuthSessionStorageHelper.class);
        this.apiClient = mock(AvanzaApiClient.class);
        this.loanFetcher = new AvanzaLoanFetcher(apiClient, authSessionStorage, temporaryStorage);
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
