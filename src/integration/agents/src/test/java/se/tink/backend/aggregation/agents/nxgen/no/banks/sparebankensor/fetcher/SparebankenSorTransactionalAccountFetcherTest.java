package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.SparebankenSorTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankenSorTransactionalAccountFetcherTest {

    private SparebankenSorApiClient apiClient;
    private SparebankenSorTransactionalAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(SparebankenSorApiClient.class);
        accountFetcher = new SparebankenSorTransactionalAccountFetcher(apiClient);
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
        assertThat(transactionalAccount.getName()).isEqualTo("Brukskonto");
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
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(1664.28);
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(1287.48);
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private List<AccountEntity> getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"list\":\n"
                                + "    [\n"
                                + "        {\n"
                                + "            \"id\": \"86011117947\",\n"
                                + "            \"accountId\": \"enc!!123456==\",\n"
                                + "            \"bban\": \"86011117947\",\n"
                                + "            \"iban\": \"NO9386011117947\",\n"
                                + "            \"cbs\": false,\n"
                                + "            \"customerRole\": \"OWNER\",\n"
                                + "            \"owner\":\n"
                                + "            {\n"
                                + "                \"name\": \"John Smith\",\n"
                                + "                \"customerNumber\": \"**HASHED:Al**\"\n"
                                + "            },\n"
                                + "            \"displayName\": \"Brukskonto\",\n"
                                + "            \"accountName\":\n"
                                + "            {\n"
                                + "                \"alias\": \"Brukskonto\",\n"
                                + "                \"accountDescription\": \"Brukskonto ung 18-34\"\n"
                                + "            },\n"
                                + "            \"properties\":\n"
                                + "            {\n"
                                + "                \"currencyCode\": \"NOK\",\n"
                                + "                \"status\": \"ACTIVE\",\n"
                                + "                \"type\": \"spending\"\n"
                                + "            },\n"
                                + "            \"accountBalance\":\n"
                                + "            {\n"
                                + "                \"availableBalance\": 1287.480,\n"
                                + "                \"accountingBalance\": 1664.280,\n"
                                + "                \"creditLine\": 0.000,\n"
                                + "                \"blockedAmount\": 0.000,\n"
                                + "                \"originalCurrencyCode\": \"NOK\"\n"
                                + "            },\n"
                                + "            \"rights\":\n"
                                + "            {\n"
                                + "                \"accessPermission\": true,\n"
                                + "                \"paymentFrom\": true,\n"
                                + "                \"transferFrom\": true,\n"
                                + "                \"transferTo\": true,\n"
                                + "                \"createPaymentAgreement\": true,\n"
                                + "                \"createAutopaymentAgreement\": true\n"
                                + "            },\n"
                                + "            \"links\":\n"
                                + "            {\n"
                                + "                \"immediatefees\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/immediatefees\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"authorizedcustomers\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/authorizedcustomers\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"payments\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/payments\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"validateclosing\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/validateclosing\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"stakeholders\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/stakeholders\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"transactions\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/transactions\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"linkedservices\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/linkedservices\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"balance\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/balance\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"freewithdrawallimitations\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/details/freewithdrawallimitations\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"self\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"details\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/details\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"interests\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/interests\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                },\n"
                                + "                \"expenses\":\n"
                                + "                {\n"
                                + "                    \"href\": \"/accounts/enc!!123456==/expenses\",\n"
                                + "                    \"verbs\":\n"
                                + "                    [\n"
                                + "                        \"GET\"\n"
                                + "                    ]\n"
                                + "                }\n"
                                + "            }\n"
                                + "        }\n"
                                + "    ]\n"
                                + "}",
                        AccountListResponse.class)
                .getAccountList();
    }
}
