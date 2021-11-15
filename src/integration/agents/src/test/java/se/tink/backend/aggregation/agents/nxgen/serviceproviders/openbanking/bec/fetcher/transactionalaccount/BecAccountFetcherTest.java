package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecAccountFetcherTest {

    private BecApiClient apiClient;
    private BecTransactionalAccountFetcher becAccountFetcher;

    @Before
    public void setup() {
        apiClient = mock(BecApiClient.class);
        becAccountFetcher = new BecTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        when(apiClient.getAccounts()).thenReturn(getFetchAccountResponse());
        when(apiClient.getBalances(any())).thenReturn(getAccountBalanceResponse());
        when(apiClient.getAccountDetails(any())).thenReturn(getAccountDetailsResponse());

        Collection<TransactionalAccount> transactionalAccounts = becAccountFetcher.fetchAccounts();

        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("00400440116243");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("00400440116243");
        assertThat(transactionalAccount.getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.IBAN
                                        && "DK5000400440116243"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "00400440116243"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(101500.0);
        assertThat(transactionalAccount.getExactAvailableBalance()).isNull();
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private GetAccountsResponse getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"resourceId\": \"resourceid12345678901234567890\",\n"
                        + "      \"iban\": \"DK5000400440116243\",\n"
                        + "      \"bban\": \"00400440116243\",\n"
                        + "      \"currency\": \"DKK\",\n"
                        + "      \"name\": \"John Smith\",\n"
                        + "      \"product\": \"SuperlÃ¸n\",\n"
                        + "      \"bic\": \"ALBADKKK\",\n"
                        + "      \"ownerName\": \"John Smith\",\n"
                        + "      \"_links\": {\n"
                        + "        \"self\": {\n"
                        + "          \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890\"\n"
                        + "        },\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890/transactions\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                GetAccountsResponse.class);
    }

    private BalancesResponse getAccountBalanceResponse() {

        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"account\": {\n"
                        + "    \"resourceId\": \"resourceid12345678901234567890\",\n"
                        + "    \"iban\": \"DK5000400440116243\"\n"
                        + "  },\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"DKK\",\n"
                        + "        \"amount\": 101500.00\n"
                        + "      },\n"
                        + "      \"balanceType\": \"closingBooked\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"DKK\",\n"
                        + "        \"amount\": 101500.00\n"
                        + "      },\n"
                        + "      \"balanceType\": \"expected\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                BalancesResponse.class);
    }

    private AccountDetailsResponse getAccountDetailsResponse() {

        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"account\": {\n"
                        + "    \"resourceId\": \"resourceid12345678901234567890\",\n"
                        + "    \"iban\": \"DK5000400440116243\",\n"
                        + "    \"bban\": \"00400440116243\",\n"
                        + "    \"currency\": \"DKK\",\n"
                        + "    \"name\": \"John Smith\",\n"
                        + "    \"product\": \"Budgetkonto\",\n"
                        + "    \"bic\": \"ALBADKKK\",\n"
                        + "    \"ownerName\": \"John Smith\",\n"
                        + "    \"_links\": {\n"
                        + "      \"self\": {\n"
                        + "        \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890\"\n"
                        + "      },\n"
                        + "      \"balances\": {\n"
                        + "        \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890/balances\"\n"
                        + "      },\n"
                        + "      \"transactions\": {\n"
                        + "        \"href\": \"/eidas/1.0/v1/accounts/resourceid12345678901234567890/transactions\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                AccountDetailsResponse.class);
    }
}
