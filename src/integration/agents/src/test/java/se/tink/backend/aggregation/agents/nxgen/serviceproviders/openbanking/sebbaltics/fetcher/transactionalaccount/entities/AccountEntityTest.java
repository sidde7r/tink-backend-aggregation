package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {

    private AccountEntity accountEntity;
    private SebBalticsApiClient apiClient;

    @Before
    public void setUp() {
        accountEntity = getAccountEntity();
        apiClient = mock(SebBalticsApiClient.class);
    }

    @Test
    public void shouldMapToTinkAccount() {
        // when
        when(apiClient.fetchAccountBalances(Mockito.anyString())).thenReturn(getBalanceResponse());
        Optional<TransactionalAccount> optionalTransactionalAccount =
                accountEntity.toTinkAccount(apiClient);

        // then
        assertTrue(optionalTransactionalAccount.isPresent());
        TransactionalAccount transactionalAccount = optionalTransactionalAccount.get();
        assertEquals("123456789", transactionalAccount.getIdModule().getUniqueId());
        assertEquals("123456789", transactionalAccount.getIdModule().getAccountNumber());
        assertEquals(
                "Firstname Lastname Banko sąskaita",
                transactionalAccount.getIdModule().getAccountName());
        assertEquals("123resourceid", transactionalAccount.getApiIdentifier());
        assertEquals(AccountTypes.CHECKING, transactionalAccount.getType());
        assertEquals(
                BigDecimal.valueOf(1852.48),
                transactionalAccount.getExactBalance().getExactValue());
        assertEquals("EUR", transactionalAccount.getExactBalance().getCurrencyCode());
        assertEquals(1, transactionalAccount.getIdentifiers().size());
        assertEquals("Firstname Lastname", transactionalAccount.getHolderName().toString());
    }

    @Test
    public void shouldThrowExceptionWhenBalanceNotAvailable() {
        // when
        when(apiClient.fetchAccountBalances(Mockito.anyString()))
                .thenReturn(getEmptyBalanceResponse());

        // then
        assertThatThrownBy(() -> accountEntity.toTinkAccount(apiClient))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not get balance");
    }

    private AccountEntity getAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "      \"resourceId\": \"123resourceid\",\n"
                        + "      \"iban\": \"123456789\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"cashAccountType\": \"currentAccount\",\n"
                        + "      \"ownerName\": \"Firstname Lastname\",\n"
                        + "      \"name\": \"Firstname Lastname Banko sąskaita\",\n"
                        + "      \"_links\": {\n"
                        + "        \"self\": {\n"
                        + "          \"href\": \"/v2/accounts\"\n"
                        + "        },\n"
                        + "        \"account\": {\n"
                        + "          \"href\": \"/v2/accounts/123resourceid\"\n"
                        + "        },\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"/v2/accounts/123resourceid/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"/v2/accounts/123resourceid/transactions\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }",
                AccountEntity.class);
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

    private BalanceResponse getEmptyBalanceResponse() {
        return SerializationUtils.deserializeFromString("{}", BalanceResponse.class);
    }
}
