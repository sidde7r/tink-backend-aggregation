package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.capitalone;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CapitalOneAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-capitalone-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("c3a19a6cacba4a0cae12c8d05c22da3f")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
