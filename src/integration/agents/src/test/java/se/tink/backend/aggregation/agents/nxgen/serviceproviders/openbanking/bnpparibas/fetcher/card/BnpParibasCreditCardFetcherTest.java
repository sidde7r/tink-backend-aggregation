package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BnpParibasCreditCardFetcherTest {

    private BnpParibasApiBaseClient bnpParibasApiBaseClient;
    private BnpParibasCreditCardFetcher objectUnderTest;
    private LocalDateTimeSource localDateTimeSource;

    @Before
    public void before() {
        bnpParibasApiBaseClient = mock(BnpParibasApiBaseClient.class);
        localDateTimeSource = mock(LocalDateTimeSource.class);

        objectUnderTest =
                new BnpParibasCreditCardFetcher(bnpParibasApiBaseClient, localDateTimeSource);
    }

    @Test
    public void shouldReturnCreditCardAccounts() {
        // given
        when(bnpParibasApiBaseClient.fetchAccounts())
                .thenReturn(BnpParibasCreditCardFetcherTestData.CREDIT_CARDS_RESPONSE);
        when(bnpParibasApiBaseClient.getBalance("31231231dqeqweqw312"))
                .thenReturn(BnpParibasCreditCardFetcherTestData.BALANCE_RESPONSE);

        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertNotNull(creditCardAccount);
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(3589.30, "EUR"));
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("card visa");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("FR3213123131231");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("FR333123311");
    }
}
