package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.manual;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SBABAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("se", "se-sbab-bankid")
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
