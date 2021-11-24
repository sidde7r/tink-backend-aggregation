package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountsResponseTest {
    SebBalticsApiClient apiClient;
    AccountsResponse accountsResponse;
    @Mock String bankBic;

    @Before
    public void setUp() {
        apiClient = mock(SebBalticsApiClient.class);
        accountsResponse = new AccountsResponse();
    }

    @Test
    public void shouldReturnEmptyListWhenEmptyAccountsResponse() {
        // given
        ReflectionTestUtils.setField(accountsResponse, "accounts", getEmptyListOfAccounts());

        // then
        Assert.assertEquals(
                Collections.emptyList(), accountsResponse.toTinkAccount(apiClient, bankBic));
    }

    @Test
    public void shouldReturnAccountsWhenBankSendsAccountList() {
        // given
        ReflectionTestUtils.setField(accountsResponse, "accounts", getAccounts());

        // when
        when(apiClient.fetchAccountBalances(Mockito.any())).thenReturn(getBalanceResponse());

        // then
        Assert.assertFalse(accountsResponse.toTinkAccount(apiClient, bankBic).isEmpty());
    }

    private List<AccountEntity> getEmptyListOfAccounts() {
        return new ArrayList<AccountEntity>();
    }

    private List<AccountEntity> getAccounts() {
        List<AccountEntity> accountEntityList = new ArrayList<>();
        AccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"resourceId\": \"123resourceid\",\n"
                                + "  \"iban\": \"123456789\",\n"
                                + "  \"currency\": \"EUR\",\n"
                                + "  \"cashAccountType\": \"currentAccount\",\n"
                                + "  \"ownerName\": \"Firstname Lastname\",\n"
                                + "  \"name\": \"Firstname Lastname Banko sąskaita\",\n"
                                + "  \"_links\": {\n"
                                + "    \"self\": {\n"
                                + "      \"href\": \"/v2/accounts\"\n"
                                + "    },\n"
                                + "    \"account\": {\n"
                                + "      \"href\": \"/v2/accounts/123resourceid\"\n"
                                + "    },\n"
                                + "    \"balances\": {\n"
                                + "      \"href\": \"/v2/accounts/123resourceid/balances\"\n"
                                + "    },\n"
                                + "    \"transactions\": {\n"
                                + "      \"href\": \"/v2/accounts/123resourceid/transactions\"\n"
                                + "    }\n"
                                + "  }\n"
                                + "}",
                        AccountEntity.class);
        accountEntityList.add(accountEntity);
        return accountEntityList;
    }

    private BalanceResponse getBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"account\": {\n"
                        + "    \"resourceId\": \"123resourceid\",\n"
                        + "    \"iban\": \"123456789\"\n"
                        + "  },\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceType\": \"interimBooked\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"1852.48\"\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"interimAvailable\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"1852.48\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"/v2/accounts/123resourceid/balances\"\n"
                        + "    },\n"
                        + "    \"account\": {\n"
                        + "      \"href\": \"/v2/accounts/123resourceid\"\n"
                        + "    },\n"
                        + "    \"transactions\": {\n"
                        + "      \"href\": \"/v2/accounts/123resourceid/transactions\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                BalanceResponse.class);
    }
}
