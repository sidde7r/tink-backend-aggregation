package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.data.NorwegianFetcherTestData;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NorwegianCardFetcherTest {

    private NorwegianCardFetcher cardFetcher;

    @Before
    public void init() {
        NorwegianApiClient client = mock(NorwegianApiClient.class);
        when(client.fetchAccounts()).thenReturn(NorwegianFetcherTestData.getAccountsResponse());
        when(client.getBalance(NorwegianFetcherTestData.ACCOUNT_2_RESOURCE_ID))
                .thenReturn(NorwegianFetcherTestData.getBalances2Response());
        cardFetcher = new NorwegianCardFetcher(client);
    }

    @Test
    public void shouldResultInExactlyOneProperlyMappedSavingsAccount() {
        // when
        List<CreditCardAccount> accounts = new ArrayList<>(cardFetcher.fetchAccounts());

        // then
        assertThat(accounts).hasSize(1);
        CreditCardAccount account = accounts.get(0);
        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getExactBalance().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.BALANCE_2));
        assertThat(account.isUniqueIdentifierEqual(NorwegianFetcherTestData.ACCOUNT_2_RESOURCE_ID))
                .isTrue();
        assertThat(account.getCardModule().getCardNumber())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_2_BBAN);
        assertThat(account.getCardModule().getCardAlias())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_2_NAME);
        assertThat(account.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_2_BBAN);
        assertThat(account.getApiIdentifier())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_2_RESOURCE_ID);
    }
}
