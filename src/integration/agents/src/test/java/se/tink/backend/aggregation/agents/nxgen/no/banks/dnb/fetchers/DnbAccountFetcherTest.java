package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbAccountFetcherTest {

    private DnbApiClient apiClient;
    private DnbAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(DnbApiClient.class);
        accountFetcher = new DnbAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        when(apiClient.fetchAccounts()).thenReturn(getFetchAccountResponse());

        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("11112222333");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("11112222333");
        assertThat(transactionalAccount.getName()).isEqualTo("Heimstaden");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "11112222333".equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(47224.64);
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(23120.64);
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private AccountListResponse getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"accountList\":\n"
                        + "    [\n"
                        + "        {\n"
                        + "            \"name\": \"Ung Brukskonto\",\n"
                        + "            \"properties\": \"closeAccount=false\",\n"
                        + "            \"hidden\": false,\n"
                        + "            \"number\": \"11112222333\",\n"
                        + "            \"owner\": \"**HASHED:jI**\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"primary\": true,\n"
                        + "            \"alias\": \"Heimstaden\",\n"
                        + "            \"bookBalance\": 47224.64,\n"
                        + "            \"availableBalance\": 23120.64,\n"
                        + "            \"bookBalanceNOK\": 47224.64,\n"
                        + "            \"transferFrom\": true,\n"
                        + "            \"payFrom\": true,\n"
                        + "            \"availableBalanceNOK\": 23120.64,\n"
                        + "            \"productNumber\": \"5543\",\n"
                        + "            \"ownerName\": \"John Smith\",\n"
                        + "            \"bsu\": false,\n"
                        + "            \"own\": true,\n"
                        + "            \"bma\": false,\n"
                        + "            \"ipa\": false,\n"
                        + "            \"grantingNOK\": 0.0,\n"
                        + "            \"granting\": 0.0,\n"
                        + "            \"delinquent\": false,\n"
                        + "            \"transferTo\": true,\n"
                        + "            \"savings\": false,\n"
                        + "            \"loan\": false,\n"
                        + "            \"taxes\": false\n"
                        + "        }"
                        + "   ]"
                        + "}",
                AccountListResponse.class);
    }
}
