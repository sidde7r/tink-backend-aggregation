package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BoursoramaCreditCardFetcherTest {

    private BoursoramaApiClient boursoramaApiClient;
    private BoursoramaCreditCardFetcher objectUnderTest;
    private LocalDateTimeSource localDateTimeSource;

    @Before
    public void before() {
        boursoramaApiClient = mock(BoursoramaApiClient.class);
        localDateTimeSource = mock(LocalDateTimeSource.class);

        objectUnderTest = new BoursoramaCreditCardFetcher(boursoramaApiClient, localDateTimeSource);
    }

    @Test
    public void shouldFetchCreditCardAccounts() {
        // given
        when(boursoramaApiClient.fetchAccounts())
                .thenReturn(BoursoramaCreditCardFetcherTestData.CARD_ACC_RESPONSE);
        when(boursoramaApiClient.fetchBalances("DCF27527D5243CD68D0FDF644744163E"))
                .thenReturn(BoursoramaCreditCardFetcherTestData.CARD_BALANCE_RES);

        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertThat(creditCardAccount).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(1642.68, "EUR"));
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("Visa classique MLE Bli Bla Blo");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("FR21312313123123");
        assertThat(creditCardAccount.getAccountNumber())
                .isEqualTo("3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D");
    }
}
