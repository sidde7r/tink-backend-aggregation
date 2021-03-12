package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataReader;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GetAccountsResponseTest {

    private static final int NO_OF_ACCOUNTS_IN_RESPONSE = 2;
    private static final int NO_OF_ACCOUNTS_AFTER_FILTERING_LOANS = 1;

    @Test
    public void parseGetAccountsResponse() {
        GetAccountsResponse getAccountsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.ACCOUNTS_RESP, GetAccountsResponse.class);

        assertThat(getAccountsResponse).isNotNull();
        assertThat(getAccountsResponse.getAccounts()).hasSize(NO_OF_ACCOUNTS_IN_RESPONSE);
        assertThat(getAccountsResponse.getAccounts().get(0).getBalance()).isEqualTo(10.0);
        assertThat(getAccountsResponse.getAccounts().get(0).getName()).isEqualTo("Basiskonto");

        List<TransactionalAccount> tinkAccounts = getAccountsResponse.getTinkAccounts();
        assertThat(tinkAccounts).isNotNull().hasSize(NO_OF_ACCOUNTS_AFTER_FILTERING_LOANS);
        assertThat(tinkAccounts.get(0).getExactBalance().getDoubleValue()).isEqualTo(10.0);
        assertThat(tinkAccounts.get(0).getName()).isEqualTo("Basiskonto");
        assertThat(tinkAccounts.get(0).getType()).isEqualTo(AccountTypes.CHECKING);
    }
}
