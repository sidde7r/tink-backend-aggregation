package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class FinTecSystemsAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("de", "de-test-fintecsystems")
                .setFinancialInstitutionId("fintecsystems")
                // This field is sometimes needed for testing sparda-west provider
                // .addCredentialField("blz-select", "36060591")
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
