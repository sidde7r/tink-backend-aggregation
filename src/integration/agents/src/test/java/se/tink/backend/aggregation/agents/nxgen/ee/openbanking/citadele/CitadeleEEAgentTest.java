package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.citadele;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CitadeleEEAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("EE", "ee-citadele-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("00824826e62711ebba800242ac130004")
                .build()
                .testRefresh();
    }
}
