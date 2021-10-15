package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SocieteGeneraleTransactionalAccountFetcherTest {

    @Mock private SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleTransactionalAccountFetcher societeGeneraleTransactionalAccountFetcher;

    @Before
    public void init() {
        societeGeneraleTransactionalAccountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldReturnProperAccount() {
        // given
        given(apiClient.fetchAccounts()).willReturn(ACCOUNTS_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(1);
        TransactionalAccount account = new ArrayList<>(accounts).get(0);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getApiIdentifier()).isEqualTo("resourceId");
        assertThat(account.getIdModule().getIdentifiers()).hasSize(1);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("FR000000000000000000000");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Compte Bancaire");
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenApiReturnsNullAccounts() {
        // given
        given(apiClient.fetchAccounts()).willReturn(null);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(0);
    }

    private static final AccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"accounts\": [\n"
                            + "    {\n"
                            + "      \"resourceId\": \"resourceId\",\n"
                            + "      \"bicFi\": \"SOGEFRPP\",\n"
                            + "      \"accountId\": {\n"
                            + "        \"iban\": \"FR000000000000000000000\"\n"
                            + "      },\n"
                            + "      \"name\": \"Compte Bancaire\",\n"
                            + "      \"usage\": \"PRIV\",\n"
                            + "      \"cashAccountType\": \"CACC\",\n"
                            + "      \"balances\": [\n"
                            + "        {\n"
                            + "          \"name\": \"Solde instantané au 14/10/2021\",\n"
                            + "          \"balanceAmount\": {\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"amount\": \"123.45\"\n"
                            + "          },\n"
                            + "          \"balanceType\": \"XPCD\",\n"
                            + "          \"lastChangeDateTime\": \"2021-10-14T00:00:00Z\",\n"
                            + "          \"referenceDate\": \"2021-10-14\"\n"
                            + "        }\n"
                            + "      ],\n"
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
                            + "    },\n"
                            + "    \"endUserIdentity\": {\n"
                            + "      \"href\": \"/end-user-identity\"\n"
                            + "    },\n"
                            + "    \"beneficiaries\": {\n"
                            + "      \"href\": \"/trusted-beneficiaries\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    AccountsResponse.class);
}
