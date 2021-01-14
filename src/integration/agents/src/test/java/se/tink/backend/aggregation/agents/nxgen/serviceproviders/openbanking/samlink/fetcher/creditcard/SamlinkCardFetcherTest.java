package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SamlinkCardFetcherTest {

    @Mock private SamlinkApiClient apiClient;
    private SamlinkCardFetcher fetcher;

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/samlink/resources";

    private static final CardsResponse EXAMPLE_CARDS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(BASE_PATH + "/cards.json"), CardsResponse.class);

    @Before
    public void setup() {
        fetcher = new SamlinkCardFetcher(apiClient);
    }

    @Test
    public void shouldFetchCardsAndConvertToTinkModel() {
        // given
        when(apiClient.fetchCardAccounts()).thenReturn(EXAMPLE_CARDS_RESPONSE);
        // when
        Collection<CreditCardAccount> creditCardAccounts = fetcher.fetchAccounts();
        // then
        assertThat(creditCardAccounts).isNotNull().hasSize(1);
        CreditCardAccount creditCardAccount = creditCardAccounts.stream().findFirst().get();
        assertThat(creditCardAccount.getName()).isEqualTo("Visa Credit/Debit");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("424397XXXXXX1111");
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of("855.68", "EUR"));
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of("144.32", "EUR"));
    }
}
