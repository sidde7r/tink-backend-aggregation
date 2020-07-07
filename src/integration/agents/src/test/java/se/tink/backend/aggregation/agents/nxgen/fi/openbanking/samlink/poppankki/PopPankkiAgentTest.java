package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.poppankki;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class PopPankkiAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-poppankki-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("5bb26ebf745d4098bed2b353389881a1")
                        .setAppId("tink")
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
