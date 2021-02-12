package se.tink.backend.aggregation.agents.nxgen.es.openbanking.sabadell;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SabadellAgentTest {

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("es", "es-redsys-sabadell-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("13079caca673456ebaf3aae93ba975b0")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
