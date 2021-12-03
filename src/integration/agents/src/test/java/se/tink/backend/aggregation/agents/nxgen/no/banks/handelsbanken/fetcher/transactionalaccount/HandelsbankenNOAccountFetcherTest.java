package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountFetchingResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenNOAccountFetcherTest {

    private HandelsbankenNOApiClient apiClient;
    private HandelsbankenNOAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(HandelsbankenNOApiClient.class);
        accountFetcher = new HandelsbankenNOAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        when(apiClient.fetchAccounts()).thenReturn(getFetchAccountResponse());

        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("86011117947");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("86011117947");
        assertThat(transactionalAccount.getName()).isEqualTo("Leif");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.IBAN
                                        && "NO9386011117947"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "86011117947".equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(117607.060);
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(117455.430);
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private List<AccountEntity> getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"list\": [\n"
                                + "    {\n"
                                + "      \"id\": \"86011117947\",\n"
                                + "      \"accountId\": \"enc!!11111-2222-333==\",\n"
                                + "      \"bban\": \"86011117947\",\n"
                                + "      \"iban\": \"NO9386011117947\",\n"
                                + "      \"cbs\": false,\n"
                                + "      \"customerRole\": \"OWNER\",\n"
                                + "      \"owner\": {\n"
                                + "        \"name\": \"John Smith\",\n"
                                + "        \"customerNumber\": \"**HASHED:+d**\"\n"
                                + "      },\n"
                                + "      \"displayName\": \"Leif\",\n"
                                + "      \"accountName\": {\n"
                                + "        \"alias\": \"Leif\",\n"
                                + "        \"accountDescription\": \"Totalkonto\"\n"
                                + "      },\n"
                                + "      \"properties\": {\n"
                                + "        \"currencyCode\": \"NOK\",\n"
                                + "        \"status\": \"ACTIVE\",\n"
                                + "        \"type\": \"spending\"\n"
                                + "      },\n"
                                + "      \"accountBalance\": {\n"
                                + "        \"availableBalance\": 117455.430,\n"
                                + "        \"accountingBalance\": 117607.060,\n"
                                + "        \"creditLine\": 0.000,\n"
                                + "        \"blockedAmount\": 0.000,\n"
                                + "        \"originalCurrencyCode\": \"NOK\"\n"
                                + "      },\n"
                                + "      \"rights\": {\n"
                                + "        \"accessPermission\": true,\n"
                                + "        \"paymentFrom\": true,\n"
                                + "        \"transferFrom\": true,\n"
                                + "        \"transferTo\": true,\n"
                                + "        \"createPaymentAgreement\": true,\n"
                                + "        \"createAutopaymentAgreement\": true\n"
                                + "      },\n"
                                + "      \"links\": {\n"
                                + "        \"immediatefees\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/immediatefees\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"transactionsSearch\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/transactions/search\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"POST\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"authorizedcustomers\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/authorizedcustomers\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"payments\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/payments\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"validateclosing\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/validateclosing\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"stakeholders\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/stakeholders\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"transactions\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/transactions\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"linkedservices\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/linkedservices\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"balance\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/balance\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"freewithdrawallimitations\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/details/freewithdrawallimitations\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"self\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"details\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/details\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"interests\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/interests\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        },\n"
                                + "        \"expenses\": {\n"
                                + "          \"href\": \"/accounts/enc!!11111-2222-333==/expenses\",\n"
                                + "          \"verbs\": [\n"
                                + "            \"GET\"\n"
                                + "          ]\n"
                                + "        }\n"
                                + "      }\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}",
                        AccountFetchingResponse.class)
                .getAccounts();
    }
}
