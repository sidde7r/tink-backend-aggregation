package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.capitalone;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CapitalOneV31AgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-capitalone-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("c3a19a6cacba4a0cae12c8d05c22da3f")
                .build()
                .testRefresh();
    }
}
