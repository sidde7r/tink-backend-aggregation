package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.SdcSeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FilterAccountsResponseTest {
    private SdcConfiguration agentConfiguration;

    @Before
    public void setUp() throws Exception {
        Provider provider = new Provider();
        provider.setMarket("SE");
        provider.setPayload("9750");
        agentConfiguration = new SdcSeConfiguration(provider);
    }

    @Test
    public void getTinkAccounts() throws Exception {
        FilterAccountsResponse response = FilterAccountsResponseTestData.getTestData();
        assertThat(response).hasSize(2);

        Collection<TransactionalAccount> accounts = response.getTinkAccounts(agentConfiguration);

        assertThat(accounts).isNotNull();
        assertThat(accounts).hasSize(1);
        for (TransactionalAccount account : accounts) {
            assertThat(account.getName()).isNotNull();
            assertThat(account.getApiIdentifier()).isNotNull();
            assertThat(account.getAccountNumber()).isNotNull();
            assertThat(account.getExactBalance().getDoubleValue()).isNotEqualTo(0);
        }
    }
}
