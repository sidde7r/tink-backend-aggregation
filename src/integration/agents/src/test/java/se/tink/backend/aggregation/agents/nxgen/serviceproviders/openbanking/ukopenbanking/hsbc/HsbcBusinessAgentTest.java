package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HsbcBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-hsbc-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("2dd34a204b31439d81c8f913044ea637")
                .build()
                .testRefresh();
    }
}
