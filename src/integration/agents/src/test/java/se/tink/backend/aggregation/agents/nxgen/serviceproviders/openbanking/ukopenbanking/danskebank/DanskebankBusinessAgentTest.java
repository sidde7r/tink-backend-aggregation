package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.danskebank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DanskebankBusinessAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-danskebank-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .setFinancialInstitutionId("a79ad0e69fbd4b7cb508ce465b1676f1")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
