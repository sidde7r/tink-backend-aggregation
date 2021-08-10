package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.santander;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SantanderAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-santander-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("8f8e9afc8be549099425617ee1a0c81d")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
