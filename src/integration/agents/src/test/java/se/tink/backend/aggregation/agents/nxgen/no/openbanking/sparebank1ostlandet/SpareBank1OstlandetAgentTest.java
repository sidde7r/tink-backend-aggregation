package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1ostlandet;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SpareBank1OstlandetAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("no", "no-sparebank1ostlandet-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("sparebank1")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
