package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.converter.CmcicCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CmcicCreditCardFetcherTest {

    private CmcicApiClient cmcicApiClient;
    private CmcicCreditCardConverter cmcicCreditCardConverter;
    private CmcicCreditCardFetcher objectUnderTest;

    @Before
    public void before() {
        cmcicApiClient = mock(CmcicApiClient.class);
        cmcicCreditCardConverter = new CmcicCreditCardConverter();
        objectUnderTest = new CmcicCreditCardFetcher(cmcicApiClient, cmcicCreditCardConverter);
    }

    @Test
    public void shouldFetchCreditCardsAccounts() {
        // given
        FetchAccountsResponse fetchAccountsResponse =
                CmcicCreditCardFetcherTestData.CREDIT_CARDS_ACCOUNT_RESPONSE;
        when(cmcicApiClient.fetchAccounts()).thenReturn(fetchAccountsResponse);

        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(231.07, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("Francua carte le credite oui");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("FR1231872361");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("FR1231872361");
    }
}
