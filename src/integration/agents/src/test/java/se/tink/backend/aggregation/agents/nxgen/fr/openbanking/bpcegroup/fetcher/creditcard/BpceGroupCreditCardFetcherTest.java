package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupCreditCardFetcherTest {

    private BpceGroupCreditCardFetcher fetcher;
    private BpceGroupApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(BpceGroupApiClient.class);
        fetcher = new BpceGroupCreditCardFetcher(apiClient);
    }

    @Test
    public void shouldFetchCards() {

        // given
        when(apiClient.fetchAccounts()).thenReturn(getAccountsResponseForCardAccount());
        when(apiClient.fetchAccountsFromCacheIfPossible())
                .thenReturn(getAccountsResponseForCardAccount());

        // when
        Collection<CreditCardAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldFilterOutNonCards() {

        // given
        when(apiClient.fetchAccounts()).thenReturn(getMultipleAccounts());
        when(apiClient.fetchAccountsFromCacheIfPossible()).thenReturn(getMultipleAccounts());

        // when
        Collection<CreditCardAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).hasSize(1);
    }

    private static AccountsResponse getAccountsResponseForCardAccount() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"connectedPsu\": \"MLLE NAME SURNAME\",\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"resourceId\": \"068-GFCidentification\",\n"
                        + "      \"bicFi\": \"CCBPFRPPGRE\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"other\": {\n"
                        + "          \"identification\": \"identification==\",\n"
                        + "          \"schemeName\": \"CPAN\",\n"
                        + "          \"issuer\": \"16807\"\n"
                        + "        }\n"
                        + "      },\n"
                        + "      \"name\": \"MLLE NAME SURNAME XX1234\",\n"
                        + "      \"details\": \"CB VISA DEBIT DIFFERE\",\n"
                        + "      \"linkedAccount\": \"068-CPT01234567890\",\n"
                        + "      \"usage\": \"PRIV\",\n"
                        + "      \"cashAccountType\": \"CARD\",\n"
                        + "      \"product\": \"Visa Classic\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"balances\": [\n"
                        + "        {\n"
                        + "          \"name\": \"Encours\",\n"
                        + "          \"balanceAmount\": {\n"
                        + "            \"currency\": \"EUR\",\n"
                        + "            \"amount\": \"610.91\"\n"
                        + "          },\n"
                        + "          \"balanceType\": \"OTHR\",\n"
                        + "          \"referenceDate\": \"2021-10-05\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "          \"name\": \"Dernier encours prélevé\",\n"
                        + "          \"balanceAmount\": {\n"
                        + "            \"currency\": \"EUR\",\n"
                        + "            \"amount\": \"1266.17\"\n"
                        + "          },\n"
                        + "          \"balanceType\": \"OTHR\",\n"
                        + "          \"referenceDate\": \"2021-09-06\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"psuStatus\": \"Account Holder\",\n"
                        + "      \"_links\": {\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"**HASHED:Qk**/stet/psd2/v1/accounts/068-GFCidentification/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"**HASHED:Qk**/stet/psd2/v1/accounts/068-GFCidentification/transactions\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"**HASHED:Qk**/stet/psd2/v1/accounts\"\n"
                        + "    },\n"
                        + "    \"first\": {\n"
                        + "      \"href\": \"**HASHED:Qk**/stet/psd2/v1/accounts\"\n"
                        + "    },\n"
                        + "    \"last\": {\n"
                        + "      \"href\": \"**HASHED:Qk**/stet/psd2/v1/accounts?page=last\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                AccountsResponse.class);
    }

    private static AccountsResponse getMultipleAccounts() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "\"accounts\": ["
                        + "    {"
                        + "      \"cashAccountType\": \"CARD\","
                        + "      \"linkedAccount\": \"ACC_NUMBER\",\n"
                        + "      \"accountId\": {"
                        + "        \"iban\": \"FR7613807008043001965409135\","
                        + "        \"other\": {"
                        + "          \"identification\": \"1234XXXX1234\","
                        + "          \"schemeName\": \"CPAN\""
                        + "        }"
                        + "      },"
                        + "      \"resourceId\": \"1234-1234\","
                        + "      \"product\": \"Visa Classic\","
                        + "      \"name\": \"NAME SURNAME XX1234\",\n"
                        + "      \"_links\": {}"
                        + "    },"
                        + "    {"
                        + "      \"cashAccountType\": \"CACC\","
                        + "      \"accountId\": {"
                        + "        \"iban\": \"FR7613807008043001965409135\""
                        + "      },"
                        + "      \"resourceId\": \"1234-4567\","
                        + "      \"name\": \"An account\","
                        + "      \"_links\": {}"
                        + "    }"
                        + "  ]"
                        + "}",
                AccountsResponse.class);
    }
}
