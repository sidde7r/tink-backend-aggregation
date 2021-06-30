package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ConsorsbankAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-consorsbank-ob")
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true)
                        .setAppId("tink")
                        .setFinancialInstitutionId("consorsbank");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
