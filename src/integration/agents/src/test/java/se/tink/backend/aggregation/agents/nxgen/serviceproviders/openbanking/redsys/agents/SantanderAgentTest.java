package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SantanderAgentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest agent =
                new AgentIntegrationTest.Builder("es", "es-redsys-santander-ob")
                        .setFinancialInstitutionId("0acbab9d2ade444faaa83f1f790f143f")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .build();

        agent.testRefresh();
    }
}
