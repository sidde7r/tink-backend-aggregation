package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HandelsbankenAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fi", "fi-handelsbanken-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("617bcf1569614498920cce658ea05aad")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
