package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ArkeaAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-creditmutueldebretagne-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("creditmutueldebretagne")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray());
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
