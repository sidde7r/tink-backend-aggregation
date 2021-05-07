package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkAccountFetcherTest {
    private static final String TEST_DATA_PATH_ACCOUNT =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/account";

    private WizinkAccountFetcher wizinkAccountFetcher;
    private WizinkStorage wizinkStorage;

    @Before
    public void setup() {
        wizinkStorage = mock(WizinkStorage.class);
        wizinkAccountFetcher = new WizinkAccountFetcher(wizinkStorage);
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNoProductsAvailable() {
        // given
        prepareData("global_position_response_without_products.json");

        // when
        Collection<TransactionalAccount> accounts = wizinkAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNoProductsInStorage() {
        // given
        when(wizinkStorage.getProductsList()).thenReturn(Collections.emptyList());

        // when
        Collection<TransactionalAccount> accounts = wizinkAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldFetchAllProducts() {
        // given
        prepareData("global_position_response.json");

        // when
        Collection<TransactionalAccount> accounts = wizinkAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
    }

    @Test
    public void shouldFetchAndMapAllProducts() {
        // given
        prepareData("global_position_response.json");

        // when
        Collection<TransactionalAccount> accounts = wizinkAccountFetcher.fetchAccounts();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertFirstAccount(iterator.next());
    }

    private void assertFirstAccount(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("5.08"));
        assertThat(account.getAccountNumber()).isEqualTo("ES96 **** **** **** **** 1309");
        assertThat(account.getName()).isEqualTo("Cuenta de ahorro WiZink");
    }

    private void prepareData(String filePath) {
        when(wizinkStorage.getProductsList())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                        Paths.get(TEST_DATA_PATH_ACCOUNT, filePath).toFile(),
                                        GlobalPositionResponse.class)
                                .getProducts());
        when(wizinkStorage.getXTokenUser())
                .thenReturn("00D4D0BEE260C666B839EFEE572461D089A3716BE117D512383E9499B44A66F2");
    }
}
