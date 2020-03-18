package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GetAccountsResponseTest {

    @Test
    public void parseGetAccountsResponse() throws Exception {
        GetAccountsResponse getAccountsResponse = GetAccountsResponseTestData.getTestData();

        assertThat(getAccountsResponse).isNotNull();
        assertThat(getAccountsResponse.getAccounts()).hasSize(2);
        assertThat(getAccountsResponse.getAccounts().get(0).getBalance()).isEqualTo(10.0);
        assertThat(getAccountsResponse.getAccounts().get(0).getName()).isEqualTo("Basiskonto");

        List<TransactionalAccount> tinkAccounts = getAccountsResponse.getTinkAccounts();
        assertThat(tinkAccounts).isNotNull();
        assertThat(tinkAccounts).hasSize(2);
        assertThat(tinkAccounts.get(0).getExactBalance().getDoubleValue()).isEqualTo(10.0);
        assertThat(tinkAccounts.get(0).getName()).isEqualTo("Basiskonto");
        assertThat(tinkAccounts.get(0).getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(tinkAccounts.get(1).getExactBalance().getDoubleValue()).isEqualTo(0.0);
        assertThat(tinkAccounts.get(1).getName()).isEqualTo("Investeringskonto");
        assertThat(tinkAccounts.get(1).getType()).isEqualTo(AccountTypes.CHECKING);
    }
}
