package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.creation;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CreationAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-creation-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("75fd6a2be882486081d0530349c56ec8")
                .build()
                .testRefresh();
    }
}
