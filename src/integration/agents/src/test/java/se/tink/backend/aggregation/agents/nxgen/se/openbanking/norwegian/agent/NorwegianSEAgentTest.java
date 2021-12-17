package se.tink.backend.aggregation.agents.nxgen.se.openbanking.norwegian.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NorwegianSEAgentTest {

    @Test
    public void testRefresh() throws Exception {

        new AgentIntegrationTest.Builder("se", "se-norwegian-ob")
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("norwegian")
                .setAppId("tink")
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
