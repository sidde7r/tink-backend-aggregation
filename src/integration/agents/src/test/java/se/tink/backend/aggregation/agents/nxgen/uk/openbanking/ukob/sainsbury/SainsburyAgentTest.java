package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.sainsbury;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SainsburyAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-sainsbury-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("cf3935993f374527a7508871a57ddbc7")
                .build()
                .testRefresh();
    }
}
