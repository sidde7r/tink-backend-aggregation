package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class KnabAccountFetcherTest {

    @Mock private KnabApiClient apiClient;

    private KnabAccountFetcher accountFetcher;

    private final KnabAccountFetcherTestFixture knabFixture = new KnabAccountFetcherTestFixture();

    @Before
    public void setUp() {
        accountFetcher = new KnabAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        bankRespondsUserHasCheckingAndSavingsAccounts();

        // when
        List<TransactionalAccount> result = new ArrayList<>(accountFetcher.fetchAccounts());

        // expect
        assertThatFetchedCheckingAndSavingsAccountsAreAsExpected(result);
    }

    @Test
    public void shouldFetchEmptyListOfAccounts() {
        // given
        bankRespondsUserHasNoAccounts();

        // when
        List<TransactionalAccount> result = new ArrayList<>(accountFetcher.fetchAccounts());

        // expect
        assertThat(result.isEmpty()).isTrue();
    }

    private void bankRespondsUserHasNoAccounts() {
        given(apiClient.fetchAccounts())
                .willReturn(knabFixture.accountsResponse("empty_accounts_response.json"));
    }

    private void bankRespondsUserHasCheckingAndSavingsAccounts() {
        given(apiClient.fetchAccounts())
                .willReturn(knabFixture.accountsResponse("accounts_response.json"));

        given(apiClient.fetchAccountBalance("586463"))
                .willReturn(knabFixture.checkingAccountBalanceResponse());

        given(apiClient.fetchAccountBalance("586464"))
                .willReturn(knabFixture.savingsAccountBalanceResponse());
    }

    private void assertThatFetchedCheckingAndSavingsAccountsAreAsExpected(
            List<TransactionalAccount> result) {
        // expect
        assertThat(result.size()).isEqualTo(2);

        // and
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(knabFixture.expectedCheckingAccount());

        // and
        assertThat(result.get(1))
                .usingRecursiveComparison()
                .isEqualTo(knabFixture.expectedSavingsAccount());
    }
}
