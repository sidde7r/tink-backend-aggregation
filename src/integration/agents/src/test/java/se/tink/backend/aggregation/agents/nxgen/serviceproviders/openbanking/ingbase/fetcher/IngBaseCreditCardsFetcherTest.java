package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngBaseCreditCardsFetcherTest {
    private static final String TEST_CURRENCY = "EUR";

    private static final FetchAccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "  \"accounts\": ["
                            + "    {"
                            + "      \"resourceId\": \"7de0041d-4f25-4b6c-a885-0bbeb1eab220\","
                            + "      \"name\": \"A. Van Dijk\","
                            + "      \"product\": \"Visa\","
                            + "      \"maskedPan\" : \"1234XXXXXX1234\","
                            + "      \"currency\": \"EUR\","
                            + "      \"_links\": {"
                            + "        \"balances\": {"
                            + "          \"href\": \"/v2/accounts/7de0041d-4f25-4b6c-a885-0bbeb1eab220/balances\""
                            + "        },"
                            + "        \"transactions\": {"
                            + "          \"href\": \"/v2/card-accounts/7de0041d-4f25-4b6c-a885-0bbeb1eab220/transactions\""
                            + "        }"
                            + "      }"
                            + "    }"
                            + "  ]"
                            + "}",
                    FetchAccountsResponse.class);

    private static final FetchBalancesResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "    \"balances\": ["
                            + "        {"
                            + "            \"balanceType\": \"closingBooked\","
                            + "            \"balanceAmount\": {"
                            + "                \"currency\": \"EUR\","
                            + "                \"amount\": 223.61"
                            + "            }"
                            + "        },"
                            + "        {"
                            + "            \"balanceType\": \"interimAvailable\","
                            + "            \"balanceAmount\": {"
                            + "                \"currency\": \"EUR\","
                            + "                \"amount\": 777.39"
                            + "            }"
                            + "        }"
                            + "    ]"
                            + "}",
                    FetchBalancesResponse.class);

    private final IngBaseApiClient apiClient = mock(IngBaseApiClient.class);

    private final IngBaseCreditCardsFetcher fetcher =
            new IngBaseCreditCardsFetcher(apiClient, TEST_CURRENCY);

    @Test
    public void shouldFetchAndMapCreditCard() {
        // given
        AccountEntity accountEntity =
                ACCOUNTS_RESPONSE.getCreditCardAccounts(TEST_CURRENCY).iterator().next();
        given(apiClient.fetchAccounts()).willReturn(ACCOUNTS_RESPONSE);
        given(apiClient.fetchBalances(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<CreditCardAccount> accounts = fetcher.fetchAccounts();

        // then
        CreditCardAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo("1234XXXXXX1234");
        assertThat(account.getName()).isEqualTo("Visa");
        assertThat(account.getHolderName()).isEqualTo(new HolderName("A. Van Dijk"));
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(223.61));
        assertThat(account.getExactAvailableCredit()).isEqualTo(ExactCurrencyAmount.inEUR(777.39));
    }
}
