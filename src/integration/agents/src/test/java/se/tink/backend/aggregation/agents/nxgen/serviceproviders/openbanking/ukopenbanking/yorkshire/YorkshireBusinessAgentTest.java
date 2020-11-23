package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.yorkshire;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class YorkshireBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-yorkshire-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("861409ea967e42cc9e5f8fc2c4732d54")
                .build()
                .testRefresh();
    }
}
