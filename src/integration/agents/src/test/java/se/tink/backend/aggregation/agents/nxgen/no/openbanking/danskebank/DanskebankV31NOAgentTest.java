package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DanskebankV31NOAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-danskebank-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("bf1f1d0ad7a04c64a32b998c4d2cd2bb")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
