package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditAgricoleCreditCardsAccountTransactionalFetcherTest {

    private PersistentStorage persistentStorage;
    private CreditAgricoleBaseApiClient creditAgricoleBaseApiClient;
    private CreditAgricoleBaseCreditCardsFetcher baseCardFetcher;
    private LocalDateTimeSource localDateTimeSource;

    @Before
    public void before() {
        persistentStorage = mock(PersistentStorage.class);
        creditAgricoleBaseApiClient = mock(CreditAgricoleBaseApiClient.class);
        localDateTimeSource = mock(LocalDateTimeSource.class);

        baseCardFetcher =
                new CreditAgricoleBaseCreditCardsFetcher(
                        creditAgricoleBaseApiClient, persistentStorage, localDateTimeSource);
    }

    @Test
    public void shouldFetchCreditCardsAccounts() {
        // given
        GetAccountsResponse getAccountsResponse =
                createFromJson(
                        CreditAgricoleCreditCardsAccountTransactionalFetcherTestData
                                .CREDIT_CARDS_ACCOUNT_RESPOMSE);
        Collection<CreditCardAccount> creditCardAccounts = getAccountsResponse.toTinkCreditCards();
        when(creditAgricoleBaseApiClient.getAccounts()).thenReturn(getAccountsResponse);

        // when
        Collection<CreditCardAccount> response = baseCardFetcher.fetchAccounts();

        // then
        CreditCardAccount expectedCreditCardAccount = response.iterator().next();
        assertNotNull(response);
        assertThat(creditCardAccounts).isEqualTo(response);
        assertThat(response.size()).isEqualTo(1);
        assertThat(expectedCreditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(response.iterator().next().getName()).isEqualTo("SAMPLE NAME");
        assertThat(expectedCreditCardAccount.getCardModule().getCardNumber())
                .isEqualTo("3123213123123123123");
        assertThat(expectedCreditCardAccount.getAccountNumber()).isEqualTo("FR3123123123123123");
    }

    private GetAccountsResponse createFromJson(String json) {
        return SerializationUtils.deserializeFromString(json, GetAccountsResponse.class);
    }
}
