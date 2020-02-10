package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class IngAgentTest {

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("be", "be-ing-ob")
                        .setFinancialInstitutionId("ing")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
