package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CreditAgricoleBaseTransactionalAccountFetcherTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ACCOUNT_WITH_ALL_LINKS =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"resourceId\": \"123123123\",\n"
                    + "            \"bicFi\": null,\n"
                    + "            \"accountId\": {\n"
                    + "                \"iban\": \"FR12312312323123123\",\n"
                    + "                \"other\": null,\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"name\": \"MONSIEUR ALLO ALLO\",\n"
                    + "            \"details\": null,\n"
                    + "            \"linkedAccount\": null,\n"
                    + "            \"usage\": \"PRIV\",\n"
                    + "            \"cashAccountType\": \"CACC\",\n"
                    + "            \"product\": \"COMPTE EKO\",\n"
                    + "            \"balances\": [\n"
                    + "                {\n"
                    + "                    \"name\": \"Accounting Balance\",\n"
                    + "                    \"balanceAmount\": {\n"
                    + "                        \"currency\": \"EUR\",\n"
                    + "                        \"amount\": \"666.66\"\n"
                    + "                    },\n"
                    + "                    \"balanceType\": \"CLBD\",\n"
                    + "                    \"lastChangeDateTime\": null,\n"
                    + "                    \"referenceDate\": \"2020-01-29\",\n"
                    + "                    \"lastCommittedTransaction\": null\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"psuStatus\": null,\n"
                    + "            \"_links\": {\n"
                    + "                \"balances\": {\n"
                    + "                    \"href\": \"/accounts/123123123/balances\",\n"
                    + "                    \"templated\": false\n"
                    + "                },\n"
                    + "                \"transactions\": {\n"
                    + "                    \"href\": \"/accounts/123123123/transactions\",\n"
                    + "                    \"templated\": true\n"
                    + "                }\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"_links\": {\n"
                    + "        \"self\": {\n"
                    + "            \"href\": \"/accounts\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"endUserIdentity\": {\n"
                    + "            \"href\": \"/end-user-identity\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"beneficiaries\": null,\n"
                    + "        \"first\": null,\n"
                    + "        \"last\": null,\n"
                    + "        \"next\": null,\n"
                    + "        \"prev\": null\n"
                    + "    }\n"
                    + "}";

    private static final String ACCOUNTS_WITHOUT_IDENTITY_LINK =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"resourceId\": \"123123123\",\n"
                    + "            \"bicFi\": null,\n"
                    + "            \"accountId\": {\n"
                    + "                \"iban\": \"FR12312312323123123\",\n"
                    + "                \"other\": null,\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"name\": \"MONSIEUR ALLO ALLO\",\n"
                    + "            \"details\": null,\n"
                    + "            \"linkedAccount\": null,\n"
                    + "            \"usage\": \"PRIV\",\n"
                    + "            \"cashAccountType\": \"CACC\",\n"
                    + "            \"product\": \"COMPTE EKO\",\n"
                    + "            \"balances\": [\n"
                    + "                {\n"
                    + "                    \"name\": \"Accounting Balance\",\n"
                    + "                    \"balanceAmount\": {\n"
                    + "                        \"currency\": \"EUR\",\n"
                    + "                        \"amount\": \"666.66\"\n"
                    + "                    },\n"
                    + "                    \"balanceType\": \"CLBD\",\n"
                    + "                    \"lastChangeDateTime\": null,\n"
                    + "                    \"referenceDate\": \"2020-01-29\",\n"
                    + "                    \"lastCommittedTransaction\": null\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"psuStatus\": null,\n"
                    + "            \"_links\": {\n"
                    + "                \"balances\": {\n"
                    + "                    \"href\": \"/accounts/123123123/balances\",\n"
                    + "                    \"templated\": false\n"
                    + "                },\n"
                    + "                \"transactions\": {\n"
                    + "                    \"href\": \"/accounts/123123123/transactions\",\n"
                    + "                    \"templated\": true\n"
                    + "                }\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"_links\": {\n"
                    + "        \"self\": {\n"
                    + "            \"href\": \"/accounts\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"endUserIdentity\": null,\n"
                    + "        \"beneficiaries\": null,\n"
                    + "        \"first\": null,\n"
                    + "        \"last\": null,\n"
                    + "        \"next\": null,\n"
                    + "        \"prev\": null\n"
                    + "    }\n"
                    + "}";

    private static final String ACCOUNT_WITHOUT_TRANSACTIONS_LINK =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"resourceId\": \"123123123\",\n"
                    + "            \"bicFi\": null,\n"
                    + "            \"accountId\": {\n"
                    + "                \"iban\": \"FR12312312323123123\",\n"
                    + "                \"other\": null,\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"name\": \"MONSIEUR ALLO ALLO\",\n"
                    + "            \"details\": null,\n"
                    + "            \"linkedAccount\": null,\n"
                    + "            \"usage\": \"PRIV\",\n"
                    + "            \"cashAccountType\": \"CACC\",\n"
                    + "            \"product\": \"COMPTE EKO\",\n"
                    + "            \"balances\": [\n"
                    + "                {\n"
                    + "                    \"name\": \"Accounting Balance\",\n"
                    + "                    \"balanceAmount\": {\n"
                    + "                        \"currency\": \"EUR\",\n"
                    + "                        \"amount\": \"666.66\"\n"
                    + "                    },\n"
                    + "                    \"balanceType\": \"CLBD\",\n"
                    + "                    \"lastChangeDateTime\": null,\n"
                    + "                    \"referenceDate\": \"2020-01-29\",\n"
                    + "                    \"lastCommittedTransaction\": null\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"psuStatus\": null,\n"
                    + "            \"_links\": {\n"
                    + "                \"balances\": {\n"
                    + "                    \"href\": \"/accounts/123123123/balances\",\n"
                    + "                    \"templated\": false\n"
                    + "                },\n"
                    + "                \"transactions\": null\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"_links\": {\n"
                    + "        \"self\": {\n"
                    + "            \"href\": \"/accounts\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"endUserIdentity\": {\n"
                    + "            \"href\": \"/end-user-identity\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"beneficiaries\": null,\n"
                    + "        \"first\": null,\n"
                    + "        \"last\": null,\n"
                    + "        \"next\": null,\n"
                    + "        \"prev\": null\n"
                    + "    }\n"
                    + "}";

    private static final String ACCOUNTS_WITHOUT_LINKS =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"resourceId\": \"123123123\",\n"
                    + "            \"bicFi\": null,\n"
                    + "            \"accountId\": {\n"
                    + "                \"iban\": \"FR12312312323123123\",\n"
                    + "                \"other\": null,\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"name\": \"MONSIEUR ALLO ALLO\",\n"
                    + "            \"details\": null,\n"
                    + "            \"linkedAccount\": null,\n"
                    + "            \"usage\": \"PRIV\",\n"
                    + "            \"cashAccountType\": \"CACC\",\n"
                    + "            \"product\": \"COMPTE EKO\",\n"
                    + "            \"balances\": [\n"
                    + "                {\n"
                    + "                    \"name\": \"Accounting Balance\",\n"
                    + "                    \"balanceAmount\": {\n"
                    + "                        \"currency\": \"EUR\",\n"
                    + "                        \"amount\": \"666.66\"\n"
                    + "                    },\n"
                    + "                    \"balanceType\": \"CLBD\",\n"
                    + "                    \"lastChangeDateTime\": null,\n"
                    + "                    \"referenceDate\": \"2020-01-29\",\n"
                    + "                    \"lastCommittedTransaction\": null\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"psuStatus\": null,\n"
                    + "            \"_links\": {\n"
                    + "                \"balances\": null,\n"
                    + "                \"transactions\": null\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"_links\": {\n"
                    + "        \"self\": {\n"
                    + "            \"href\": \"/accounts\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"endUserIdentity\": null,\n"
                    + "        \"beneficiaries\": null,\n"
                    + "        \"first\": null,\n"
                    + "        \"last\": null,\n"
                    + "        \"next\": null,\n"
                    + "        \"prev\": null\n"
                    + "    }\n"
                    + "}";

    private CreditAgricoleBaseApiClient apiClient;
    private CreditAgricoleBaseTransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void before() {
        apiClient = mock(CreditAgricoleBaseApiClient.class);
        transactionalAccountFetcher = new CreditAgricoleBaseTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccountsWithNoNecessaryConsents() {
        // given
        GetAccountsResponse accountsResponse = createFromJson(ACCOUNT_WITH_ALL_LINKS);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        when(apiClient.getAccounts()).thenReturn(accountsResponse);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(1)).getAccounts();
        verify(apiClient, never()).putConsents(any());
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenTransactionsLinkMissing() {
        // given
        GetAccountsResponse accountsResponse = createFromJson(ACCOUNT_WITHOUT_TRANSACTIONS_LINK);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenIdentityLinkMissing() {
        // given
        GetAccountsResponse accountsResponse = createFromJson(ACCOUNTS_WITHOUT_IDENTITY_LINK);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenLinksMissing() {
        // given
        GetAccountsResponse accountsResponse = createFromJson(ACCOUNTS_WITHOUT_LINKS);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    private GetAccountsResponse createFromJson(String json) {
        try {
            return MAPPER.readValue(json, GetAccountsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
