package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class N26AgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("fr", "fr-n26-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("n26")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .build()
                .testRefresh();
    }
}
