package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.creditcard.LclCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LclCreditCardFetcherTest {

    private LclApiClient lclApiClient;
    private LclDataConverter lclDataConverter;
    private LclCreditCardFetcher objectUnderTest;

    @Before
    public void before() {
        lclApiClient = mock(LclApiClient.class);
        lclDataConverter = mock(LclDataConverter.class);
        objectUnderTest = new LclCreditCardFetcher(lclApiClient, lclDataConverter);
    }

    @Test
    public void shouldFetchCreditCardsAccounts() {
        // given
        when(lclApiClient.getAccountsResponse())
                .thenReturn(LclCreditCardFetcherTestData.ACCOUNTS_CARDS_RESPONSE);

        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(100, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("CREDITE CARTE EL FRANCUSO");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("12345");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("31232141");
    }
}
