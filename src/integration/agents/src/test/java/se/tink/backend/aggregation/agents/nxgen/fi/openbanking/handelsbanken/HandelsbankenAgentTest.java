package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HandelsbankenAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-handelsbanken-ob")
                        .addCredentialField("accessToken", "MV9QUk9GSUxFLUZJX1BSSVZBVEUz")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("617bcf1569614498920cce658ea05aad")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
