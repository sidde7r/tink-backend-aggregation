package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.norwegian.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NorwegianDKAgentTest {

    @Test
    public void testRefresh() throws Exception {

        new AgentIntegrationTest.Builder("dk", "dk-norwegian-ob")
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("norwegian")
                .setAppId("tink")
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .build()
                .testRefresh();
    }
}
