package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFetcherTest {

    private static final String CREDIT_CARD_ACCOUNTS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/creditCardAccounts.json";

    private static final String CREDIT_CARD_DETAILS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/creditCardDetails.json";

    private static final CreditCardsResponse CREDIT_CARD_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(CREDIT_CARD_ACCOUNTS_FILE_PATH), CreditCardsResponse.class);
    private static final CreditCardDetailsResponse CREDIT_CARD_DETAILS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(CREDIT_CARD_DETAILS_FILE_PATH), CreditCardDetailsResponse.class);

    private static final String MASKED_PAN = "5269 **** **** 3239";

    @Test
    public void shouldReturnProperlyMappedCreditCards() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchCreditCards()).willReturn(CREDIT_CARD_ACCOUNTS_RESPONSE);
        given(fetcherClient.fetchCreditCardDetails("2218836201"))
                .willReturn(CREDIT_CARD_DETAILS_RESPONSE);
        CreditCardFetcher creditCardFetcher = new CreditCardFetcher(fetcherClient);
        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then

        assertThat(creditCardAccounts).hasSize(1);
        CreditCardAccount card = creditCardAccounts.iterator().next();
        assertThat(card.isUniqueIdentifierEqual(MASKED_PAN)).isTrue();
        assertThat(card.getApiIdentifier()).isEqualTo("2218836201");
        assertThat(card.getIdentifiers()).hasSize(1);
        assertThat(card.getIdentifiers().get(0).getType())
                .isEqualTo(AccountIdentifierType.PAYMENT_CARD_NUMBER);
        assertThat(card.getIdentifiers().get(0).getIdentifier()).isEqualTo(MASKED_PAN);

        assertThat(card.getName()).isEqualTo(MASKED_PAN);
        assertThat(card.getAccountNumber()).isEqualTo(MASKED_PAN);

        assertThat(card.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(127.05, "NOK"));
        assertThat(card.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(1765.06, "NOK"));
    }
}
