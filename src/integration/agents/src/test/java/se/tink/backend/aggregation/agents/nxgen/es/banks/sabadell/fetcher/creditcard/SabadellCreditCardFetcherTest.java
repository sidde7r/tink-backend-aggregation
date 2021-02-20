package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellCreditCardFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/resources";

    private SabadellApiClient sabadellApiClient;
    private SabadellCreditCardFetcher creditCardFetcher;

    @Before
    public void setup() {
        sabadellApiClient = mock(SabadellApiClient.class);
        creditCardFetcher = new SabadellCreditCardFetcher(sabadellApiClient);
    }

    @Test
    public void shouldReturnExactNumberOfCreditCardsAccounts() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_correct_response.json")
                                        .toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(2);
    }

    @Test
    public void shouldReturnEmptyListWhenResponseDoesNotContainAccounts() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_null_list_response.json")
                                        .toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).isEmpty();
    }

    @Test
    public void shouldReturnOnlyCreditCards() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_different_cards.json").toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        Iterator<CreditCardAccount> iterator = creditCardAccounts.iterator();
        CreditCardAccount next = iterator.next();
        assertThat(next.getType()).isEqualTo(AccountTypes.CREDIT_CARD);

        assertThat(creditCardAccounts).hasSize(1);
    }
}
