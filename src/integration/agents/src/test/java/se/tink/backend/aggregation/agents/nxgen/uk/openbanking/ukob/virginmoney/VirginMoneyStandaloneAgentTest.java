package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.virginmoney;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class VirginMoneyStandaloneAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-virginmoney-standalone-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("8053cad9d41d467680be7250d830075d")
                .build()
                .testRefresh();
    }
}
