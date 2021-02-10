package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.rpc.CreditCardAccountsListResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1CreditCardFetcherTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Test
    public void fetchAccountShouldReturnActiveCreditCards() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        Sparebank1ApiClient apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        Sparebank1CreditCardFetcher fetcher = new Sparebank1CreditCardFetcher(apiClient);
        when(requestBuilder.get(CreditCardAccountsListResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "creditcard_response.json").toFile(),
                                CreditCardAccountsListResponse.class));

        // when
        Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();

        // then
        assertThat(creditCards).hasSize(1);
        CreditCardAccount creditCard = creditCards.iterator().next();
        assertThat(creditCard.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(9643.95), "NOK"));
        assertThat(creditCard.getCardModule().getBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(-356.05), "NOK"));
        assertThat(creditCard.getApiIdentifier()).isEqualTo("dummyId");
        assertThat(creditCard.getName()).isEqualTo("dummyName");
        assertThat(creditCard.getIdModule().getUniqueId()).isEqualTo("dummyId");
        assertThat(creditCard.getIdModule().getAccountNumber()).isEqualTo("123456789");
    }
}
