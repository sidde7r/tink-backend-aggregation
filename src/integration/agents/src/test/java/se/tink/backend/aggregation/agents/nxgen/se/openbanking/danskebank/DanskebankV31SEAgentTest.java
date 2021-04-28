package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DanskebankV31SEAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "se-danskebank-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("danskebank")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
