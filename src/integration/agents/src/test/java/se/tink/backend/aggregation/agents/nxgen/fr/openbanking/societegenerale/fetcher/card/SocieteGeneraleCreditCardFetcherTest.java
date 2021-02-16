package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SocieteGeneraleCreditCardFetcherTest {

    private SocieteGeneraleApiClient societeGeneraleApiClient;
    private SocieteGeneraleCreditCardFetcher objectUnderTest;

    @Before
    public void before() {
        societeGeneraleApiClient = mock(SocieteGeneraleApiClient.class);
        objectUnderTest = new SocieteGeneraleCreditCardFetcher(societeGeneraleApiClient);
    }

    @Test
    public void shouldReturnCreditCards() {
        // given
        when(societeGeneraleApiClient.fetchAccounts())
                .thenReturn(SocieteGeneraleCreditCardFetcherTestData.CARD_RESPONSE);
        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("CB Mastercard");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("213123213123");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("9832749872938943875938");
    }
}
