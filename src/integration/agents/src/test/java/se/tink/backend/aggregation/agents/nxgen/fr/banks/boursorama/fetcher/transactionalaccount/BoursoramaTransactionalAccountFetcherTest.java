package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaTransactionalAccountFetcherTest {

    private static final String ACCOUNTS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/boursorama/resources/transactional_accounts.json";

    private BoursoramaTransactionalAccountFetcher accountFetcher;
    private BoursoramaApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(BoursoramaApiClient.class);
        accountFetcher = new BoursoramaTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        givenApiClientWillReturnAccounts();
        // when
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();
        // then
        assertThat(transactionalAccounts).hasSize(2);
        assertThatResultContainsExpectedCheckingAccount(transactionalAccounts);
        assertThatResultContainsExpectedSavingsAccount(transactionalAccounts);
    }

    private void assertThatResultContainsExpectedSavingsAccount(
            Collection<TransactionalAccount> transactionalAccounts) {
        Optional<TransactionalAccount> savingsAccount =
                transactionalAccounts.stream()
                        .filter(a -> AccountTypes.SAVINGS.equals(a.getType()))
                        .findAny();
        assertThat(savingsAccount).isPresent();
        assertThat(savingsAccount.get().getExactBalance().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(15.18));
        assertThat(savingsAccount.get().getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(savingsAccount.get().getIdModule().getUniqueId())
                .isEqualTo("FR7640618803000002163936859");
    }

    private void assertThatResultContainsExpectedCheckingAccount(
            Collection<TransactionalAccount> transactionalAccounts) {
        Optional<TransactionalAccount> checkingAccount =
                transactionalAccounts.stream()
                        .filter(a -> AccountTypes.CHECKING.equals(a.getType()))
                        .findAny();
        assertThat(checkingAccount).isPresent();
        assertThat(checkingAccount.get().getExactBalance().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(6970.75));
        assertThat(checkingAccount.get().getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(checkingAccount.get().getIdModule().getUniqueId())
                .isEqualTo("FR7640618803000004037248195");
    }

    private void givenApiClientWillReturnAccounts() {
        when(apiClient.getAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(ACCOUNTS_FILE_PATH), ListAccountsResponse.class));
    }
}
