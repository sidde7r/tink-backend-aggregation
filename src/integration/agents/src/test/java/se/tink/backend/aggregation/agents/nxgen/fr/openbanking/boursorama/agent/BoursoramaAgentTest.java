package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class BoursoramaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-boursorama-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("boursorama")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.TRANSFER_DESTINATIONS)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray());
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
