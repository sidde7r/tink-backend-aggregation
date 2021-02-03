package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard;

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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoCreditCardFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";
    private static final String ACCOUNT_NUMBER_1 = "9999 **** **** 6999";
    private static final String ACCOUNT_NUMBER_2 = "6367 **** **** 4455";

    private EvoBancoApiClient evoBancoApiClient;
    private EvoBancoCreditCardFetcher accountFetcher;

    @Before
    public void setup() {
        evoBancoApiClient = mock(EvoBancoApiClient.class);
        accountFetcher = new EvoBancoCreditCardFetcher(evoBancoApiClient);
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
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<CreditCardAccount> iterator = accounts.iterator();
        assertCreditCardAccount1(iterator.next());
        assertCreditCardAccount2(iterator.next());
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
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
    }

    private void assertCreditCardAccount1(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("58.05"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("56.79"));
        assertThat(account.getHolders().get(0).getName()).isEqualTo("SZYMON MYSIAK");
        assertThat(account.getFromTemporaryStorage(EvoBancoConstants.Storage.CARD_STATE))
                .isEqualTo("PODER CLI.");
        assertThat(account.getAccountNumber()).isEqualTo("9999 **** **** 6999");
        assertThat(account.getName()).isEqualTo("TARJETA DE DÉBITO *6999");
    }

    private void assertCreditCardAccount2(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("546.56"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(account.getHolders().get(0).getName()).isEqualTo("SZYMON MYSIAK");
        assertThat(account.getFromTemporaryStorage(EvoBancoConstants.Storage.CARD_STATE))
                .isEqualTo("E.EST.RENO");
        assertThat(account.getAccountNumber()).isEqualTo("6367 **** **** 4455");
        assertThat(account.getName()).isEqualTo("TARJETA MIXTA *4455");
    }
}
