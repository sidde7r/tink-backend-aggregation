package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LaBanquePostaleCardFetcherTest {

    private LaBanquePostaleApiClient laBanquePostaleApiClient;
    private LaBanquePostaleCardFetcher objectUnderTest;

    @Before
    public void before() {
        laBanquePostaleApiClient = mock(LaBanquePostaleApiClient.class);
        objectUnderTest = new LaBanquePostaleCardFetcher(laBanquePostaleApiClient);
    }

    @Test
    public void shouldFetchCreditCards() {
        // given
        given(laBanquePostaleApiClient.fetchAccounts())
                .willReturn(LaBanquePostaleCardFetcherTestData.ACCOUNTS_RESPONSE_WITH_BALANCE);

        // when
        Collection<CreditCardAccount> accounts = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = accounts.iterator().next();
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("321321321312");
        assertThat(creditCardAccount.getName()).isEqualTo("visa123");
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.inEUR(821.25));
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.inEUR(0.0));
    }
}
