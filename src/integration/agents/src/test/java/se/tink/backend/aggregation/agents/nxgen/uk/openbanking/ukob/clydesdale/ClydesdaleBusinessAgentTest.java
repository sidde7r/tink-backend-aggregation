package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.clydesdale;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ClydesdaleBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-clydesdale-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("cd8880ce4776431fb19d7a80c3cb507a")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
