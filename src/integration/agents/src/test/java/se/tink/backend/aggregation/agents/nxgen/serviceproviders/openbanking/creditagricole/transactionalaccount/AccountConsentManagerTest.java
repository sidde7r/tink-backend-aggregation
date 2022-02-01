package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;

public class AccountConsentManagerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CreditAgricoleBaseApiClient apiClient;
    private AccountConsentManager accountConsentManager;

    @Before
    public void setUp() {
        apiClient = mock(CreditAgricoleBaseApiClient.class);
        accountConsentManager = new AccountConsentManager(apiClient);
    }

    @Test
    public void shouldPrepareConsents() {
        // given
        GetAccountsResponse accountsResponse =
                fromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNTS_WITHOUT_LINKS);
        doNothing()
                .when(apiClient)
                .putConsents(accountsResponse.getAccountsListForNecessaryConsents());

        // when
        boolean result = accountConsentManager.prepareConsentsIfNeeded(accountsResponse);

        // then
        Assertions.assertThat(result).isTrue();
        verify(apiClient).putConsents(accountsResponse.getAccountsListForNecessaryConsents());
    }

    @Test
    public void shouldPrepareConsentsOnlyOnce() {
        // given
        GetAccountsResponse accountsResponse =
                fromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNTS_WITHOUT_LINKS);
        doNothing()
                .when(apiClient)
                .putConsents(accountsResponse.getAccountsListForNecessaryConsents());

        // when
        boolean firstResult = accountConsentManager.prepareConsentsIfNeeded(accountsResponse);
        boolean secondResult = accountConsentManager.prepareConsentsIfNeeded(accountsResponse);

        // then
        Assertions.assertThat(firstResult).isTrue();
        Assertions.assertThat(secondResult).isFalse();
        verify(apiClient).putConsents(accountsResponse.getAccountsListForNecessaryConsents());
    }

    @Test
    public void shouldNotPrepareConsents() {
        // given
        GetAccountsResponse accountsResponse =
                fromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNT_WITH_ALL_LINKS);
        doNothing()
                .when(apiClient)
                .putConsents(accountsResponse.getAccountsListForNecessaryConsents());

        // when
        boolean result = accountConsentManager.prepareConsentsIfNeeded(accountsResponse);

        // then
        Assertions.assertThat(result).isFalse();
        verifyNoInteractions(apiClient);
    }

    @Test
    public void shouldThrowExceptionIfAccountResponseIsNull() {
        // when
        Throwable throwable =
                catchThrowable(() -> accountConsentManager.prepareConsentsIfNeeded(null));

        // then
        assertThat(throwable)
                .isInstanceOf(NullPointerException.class)
                .hasMessage("accountsResponse is marked non-null but is null");
        verifyNoInteractions(apiClient);
    }

    private GetAccountsResponse fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, GetAccountsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
