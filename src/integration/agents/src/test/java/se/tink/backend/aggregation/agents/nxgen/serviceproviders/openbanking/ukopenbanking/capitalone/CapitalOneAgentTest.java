package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.capitalone;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CapitalOneAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-capitalone-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("capitalone")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
