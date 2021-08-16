package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.CreditCardTestData;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;

public class NordeaCreditCardFetcherTest {

    private NordeaDkApiClientMockWrapper apiClientMockWrapper;
    private NordeaCreditCardFetcher fetcher;

    @Before
    public void before() {
        NordeaDkApiClient apiClientMock = mock(NordeaDkApiClient.class);
        apiClientMockWrapper = new NordeaDkApiClientMockWrapper(apiClientMock);
        fetcher = new NordeaCreditCardFetcher(apiClientMock);
    }

    @Test
    public void shouldFetchCreditCards() {
        // given
        apiClientMockWrapper.mockFetchCreditCardsUsingFile(CreditCardTestData.CREDIT_CARDS_FILE);
        apiClientMockWrapper.mockFetchCreditCardDetailsUsingFile(
                CreditCardTestData.CREDIT_CARD_ID, CreditCardTestData.CREDIT_CARD_DETAILS_FILE);

        // when
        Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();

        // then
        assertThat(creditCards).hasSize(1);
        // and
        Optional<CreditCardAccount> creditCard =
                creditCards.stream()
                        .filter(a -> "5678".equals(a.getIdModule().getUniqueId()))
                        .findAny();
        assertThat(creditCard.isPresent()).isTrue();
        assertThat(creditCard.get().getIdModule().getUniqueId()).isEqualTo("5678");
        assertThat(creditCard.get().getIdModule().getAccountNumber())
                .isEqualTo("1234 **** **** 5678");
        assertThat(creditCard.get().getCardModule().getCardNumber())
                .isEqualTo("1234 **** **** 5678");
        assertThat(creditCard.get().getCardModule().getBalance().getCurrencyCode())
                .isEqualTo("DKK");
        assertThat(creditCard.get().getCardModule().getBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(creditCard.get().getCardModule().getAvailableCredit().getCurrencyCode())
                .isEqualTo("DKK");
        assertThat(creditCard.get().getCardModule().getAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(creditCard.get().getIdentifiers().size()).isEqualTo(1);
        assertThat(creditCard.get().getIdentifiers().get(0))
                .isEqualTo(new MaskedPanIdentifier("1234********5678"));
    }
}
