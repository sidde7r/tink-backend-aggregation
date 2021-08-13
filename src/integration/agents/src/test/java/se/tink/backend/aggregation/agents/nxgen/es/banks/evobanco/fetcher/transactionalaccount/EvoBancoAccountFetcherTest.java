package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoAccountFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";
    private static final String HOLDER_NAME = "SZYMON MYSIAK";

    private EvoBancoApiClient evoBancoApiClient;
    private EvoBancoAccountFetcher accountFetcher;

    @Before
    public void setup() {
        evoBancoApiClient = mock(EvoBancoApiClient.class);
        SessionStorage sessionStorage = new SessionStorage();
        sessionStorage.put(EvoBancoConstants.Storage.HOLDER_NAME, HOLDER_NAME);
        accountFetcher = new EvoBancoAccountFetcher(evoBancoApiClient, sessionStorage);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        when(evoBancoApiClient.globalPosition())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_correct_response.json")
                                        .toFile(),
                                GlobalPositionResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertSavingAccountValid(iterator.next());
        assertCheckingAccountValid(iterator.next());
    }

    @Test
    public void shouldReturnOnlyAccountsWithKnownAccountType() {
        // given
        when(evoBancoApiClient.globalPosition())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_correct_response.json")
                                        .toFile(),
                                GlobalPositionResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
    }

    private void assertSavingAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.SAVINGS.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("18.03"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("IBAN1");
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo("IBAN1");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Depósito");
        assertThat(account.getIdModule().getProductName()).isEqualTo("I");
        assertThat(account.getParties().get(0).getName()).isEqualTo("SZYMON MYSIAK");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Role.HOLDER);
    }

    private void assertCheckingAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.CHECKING.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("3189.06"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("IBAN2");
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo("IBAN2");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Cuenta Inteligente");
        assertThat(account.getIdModule().getProductName()).isEqualTo("I");
        assertThat(account.getParties().get(0).getName()).isEqualTo("SZYMON MYSIAK");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Role.HOLDER);
    }
}
