package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HandelsbankenAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-handelsbanken-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("7c1d90cabd244db0b42dd826e6f87d31")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
