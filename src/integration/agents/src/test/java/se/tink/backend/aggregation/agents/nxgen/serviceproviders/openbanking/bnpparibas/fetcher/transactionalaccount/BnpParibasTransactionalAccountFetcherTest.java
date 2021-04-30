package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BnpParibasTransactionalAccountFetcherTest {

    private BnpParibasApiBaseClient apiClient;
    private AccountsResponse accountsResponse;

    @Before
    public void init() {
        apiClient = mock(BnpParibasApiBaseClient.class);
        accountsResponse = mock(AccountsResponse.class);
    }

    @Test
    public void shouldReturnEmptyCollectionOnFetchAccounts() {
        // given
        when(accountsResponse.getAccounts()).thenReturn(null);
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);

        BnpParibasTransactionalAccountFetcher bnpParibasTransactionalAccountFetcher =
                new BnpParibasTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> resp =
                bnpParibasTransactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(resp).isNotNull();
        assertThat(resp).isEmpty();
    }

    @Test
    public void shouldReturnObjectOnFetchAccounts() {
        // given
        when(apiClient.getBalance(anyString())).thenReturn(loadBalanceResponse());
        when(apiClient.fetchAccounts()).thenReturn(loadAccountsResponse());

        BnpParibasTransactionalAccountFetcher bnpParibasTransactionalAccountFetcher =
                new BnpParibasTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> resp =
                bnpParibasTransactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(resp).isNotNull();
        assertThat(resp).isNotEmpty();
        TransactionalAccount account = new ArrayList<>(resp).get(0);
        assertThat(account.getName()).isEqualTo("Compte de chèques");
        assertThat(account.getAccountNumber()).isEqualTo("iban");
        assertThat(account.getApiIdentifier()).isEqualTo("resourceId");
        assertThat(account.getExactBalance().getExactValue()).isEqualTo(BigDecimal.valueOf(24.55));
    }

    private BalanceResponse loadBalanceResponse() {
        String data =
                "{\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"name\": \"SOLDE COMPTABLE au 29/04/2021\",\n"
                        + "      \"referenceDate\": \"2021-04-29\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"24.55\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"CLBD\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"/accounts/resourceId/balances\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}";
        return SerializationUtils.deserializeFromString(data, BalanceResponse.class);
    }

    private AccountsResponse loadAccountsResponse() {
        String data =
                "{\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"resourceId\": \"resourceId\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"iban\": \"iban\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"name\": \"Compte de chèques ****1234\",\n"
                        + "      \"details\": \"Bnp Paribas\",\n"
                        + "      \"usage\": \"PRIV\",\n"
                        + "      \"cashAccountType\": \"CACC\",\n"
                        + "      \"psuStatus\": \"Account Holder\",\n"
                        + "      \"_links\": {\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"/accounts/resourceId/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"/accounts/resourceId/transactions\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"/accounts\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}";
        return SerializationUtils.deserializeFromString(data, AccountsResponse.class);
    }
}
