package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;

public class CbiAccountFetchingStepTest {

    private CbiGlobeFetcherApiClient mockFetcherApiClient;
    private CbiStorage mockStorage;

    private CbiAccountFetchingStep accountFetchingStep;

    @Before
    public void setup() {
        mockFetcherApiClient = mock(CbiGlobeFetcherApiClient.class);
        mockStorage = mock(CbiStorage.class);

        accountFetchingStep = new CbiAccountFetchingStep(mockFetcherApiClient, mockStorage);
    }

    @Test
    public void shouldFetchAccountsAndSaveResponseToStorageWithoutChangesWhenAlLAccountsOk() {
        // given
        AccountsResponse accountsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.FETCH_ACCOUNTS_OK, AccountsResponse.class);
        when(mockFetcherApiClient.getAccounts()).thenReturn(accountsResponse);

        // when
        accountFetchingStep.fetchAndSaveAccounts();

        // then
        verify(mockStorage).saveAccountsResponse(accountsResponse);
    }

    @Test
    public void shouldPruneAccountsWithTooLittleDataBeforeSaving() {
        // given
        AccountsResponse accountsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.FETCH_ACCOUNTS_WITH_SOME_USELESS, AccountsResponse.class);
        when(mockFetcherApiClient.getAccounts()).thenReturn(accountsResponse);

        AccountsResponse expectedSavedAccounts =
                new AccountsResponse(
                        Arrays.asList(
                                accountsResponse.getAccounts().get(1),
                                accountsResponse.getAccounts().get(3)));

        // when
        accountFetchingStep.fetchAndSaveAccounts();

        // then
        verify(mockStorage).saveAccountsResponse(expectedSavedAccounts);
    }
}
