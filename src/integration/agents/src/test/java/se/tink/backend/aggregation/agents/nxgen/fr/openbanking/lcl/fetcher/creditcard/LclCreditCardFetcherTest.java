package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.creditcard.LclCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LclCreditCardFetcherTest {

    private LclApiClient lclApiClient;
    private LclDataConverter lclDataConverter;
    private LclCreditCardFetcher lclCreditCardFetcher;

    @Before
    public void before() {
        lclApiClient = mock(LclApiClient.class);
        lclDataConverter = mock(LclDataConverter.class);
        lclCreditCardFetcher = new LclCreditCardFetcher(lclApiClient, lclDataConverter);
    }

    @Test
    public void shouldFetchCreditCardsAccounts() {
        // given
        AccountsResponseDto accountsResponseDto =
                createFromJson(LclCreditCardFetcherTestData.ACCOUNTS_CARDS_RESPONSE);
        when(lclApiClient.getAccountsResponse()).thenReturn(accountsResponseDto);
        Collection<CreditCardAccount> creditCardAccounts = lclCreditCardFetcher.fetchAccounts();

        // when
        Collection<CreditCardAccount> response = lclCreditCardFetcher.fetchAccounts();

        // then
        CreditCardAccount expected = response.iterator().next();
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(creditCardAccounts);
        assertThat(response.size()).isEqualTo(1);
        assertThat(expected.getExactAvailableCredit()).isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(expected.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(100, "EUR"));
        assertThat(expected.getName()).isEqualTo("CREDITE CARTE EL FRANCUSO");
        assertThat(expected.getCardModule().getCardNumber()).isEqualTo("12345");
        assertThat(expected.getAccountNumber()).isEqualTo("31232141");
    }

    private AccountsResponseDto createFromJson(String json) {
        return SerializationUtils.deserializeFromString(json, AccountsResponseDto.class);
    }
}
