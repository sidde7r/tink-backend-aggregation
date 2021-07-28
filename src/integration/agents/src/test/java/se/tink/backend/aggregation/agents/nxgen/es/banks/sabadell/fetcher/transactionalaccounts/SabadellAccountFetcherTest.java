package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellAccountFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/resources/";
    private static final String FIRST_ACCOUNT_IBAN = "ES0820952954871624915582";
    private static final String SECOND_ACCOUNT_IBAN = "ES3831906561918657847255";

    private SabadellApiClient apiClient;
    private SabadellAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(SabadellApiClient.class);
        accountFetcher = new SabadellAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        when(apiClient.fetchAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                                AccountsResponse.class));
        when(apiClient.fetchAccountHolders(Mockito.any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "account_holders.json").toFile(),
                                AccountHoldersResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        TransactionalAccount firstAccount =
                accounts.stream()
                        .filter(
                                account ->
                                        FIRST_ACCOUNT_IBAN.equals(
                                                account.getIdModule().getUniqueId()))
                        .findFirst()
                        .orElse(null);
        assertFirstAccountValid(firstAccount);
        // and
        TransactionalAccount secondAccount =
                accounts.stream()
                        .filter(
                                account ->
                                        SECOND_ACCOUNT_IBAN.equals(
                                                account.getIdModule().getUniqueId()))
                        .findFirst()
                        .orElse(null);
        assertSecondAccountValid(secondAccount);
    }

    private void assertFirstAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.CHECKING.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("13591.73"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("ES0820952954871624915582");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CUENTA RELACIÓN");
        assertThat(account.getParties().get(0).getName()).isEqualTo("Owner Dupa");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Role.HOLDER);
        assertThat(account.getParties().size()).isGreaterThan(1);
    }

    private void assertSecondAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.CHECKING.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("79.32"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("ES3831906561918657847255");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CUENTA EXPANSIÓN");
        assertThat(account.getParties().get(1).getName()).isEqualTo("Authorized Dupa");
        assertThat(account.getParties().get(1).getRole()).isEqualTo(Role.AUTHORIZED_USER);
        assertThat(account.getParties().size()).isGreaterThan(1);
    }
}
