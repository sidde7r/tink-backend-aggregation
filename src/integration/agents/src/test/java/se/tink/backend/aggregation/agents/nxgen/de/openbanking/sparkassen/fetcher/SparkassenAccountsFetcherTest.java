package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SparkassenAccountsFetcherTest {

    private static final String TEST_CONSENT_ID = "CONSENT_ID";

    private SparkassenApiClient apiClient;
    private SparkassenStorage persistentStorage;
    private SparkassenAccountsFetcher accountsFetcher;

    @Before
    public void setup() {
        apiClient = mock(SparkassenApiClient.class);
        persistentStorage = new SparkassenStorage(new PersistentStorage());
        persistentStorage.saveConsentId(TEST_CONSENT_ID);
        accountsFetcher = new SparkassenAccountsFetcher(apiClient, persistentStorage);

        when(apiClient.getAccountBalance(any(), anyString()))
                .thenReturn(
                        FetcherTestData.getFetchBalancesResponse(
                                "EUR", BigDecimal.valueOf(123.45)));
    }

    @Test
    public void shouldReturnExactlyFiveAccounts() {
        // given
        FetchAccountsResponse fetchAccountsResponse = FetcherTestData.getFetchAccountsResponse(5);
        when(apiClient.fetchAccounts(TEST_CONSENT_ID)).thenReturn(fetchAccountsResponse);

        // when
        Collection<TransactionalAccount> accounts = accountsFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(5);
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNoAccountsRetrievedFromBank() {
        // given
        FetchAccountsResponse fetchAccountsResponse = FetcherTestData.getFetchAccountsResponse(0);
        when(apiClient.fetchAccounts(TEST_CONSENT_ID)).thenReturn(fetchAccountsResponse);

        // when
        Collection<TransactionalAccount> accounts = accountsFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(0);
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNullAccountReturnedFromBank() {
        // given
        FetchAccountsResponse fetchAccountsResponse = FetcherTestData.NULL_ACCOUNTS;
        when(apiClient.fetchAccounts(TEST_CONSENT_ID)).thenReturn(fetchAccountsResponse);

        // when
        Collection<TransactionalAccount> accounts = accountsFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(0);
    }
}
