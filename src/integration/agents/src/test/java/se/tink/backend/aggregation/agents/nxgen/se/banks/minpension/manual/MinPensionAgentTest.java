package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.manual;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class MinPensionAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("se", "se-minpension-bankid")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
