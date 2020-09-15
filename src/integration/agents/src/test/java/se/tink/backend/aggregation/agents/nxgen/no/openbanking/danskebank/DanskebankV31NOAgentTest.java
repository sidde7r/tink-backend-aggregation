package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DanskebankV31NOAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-danskebank-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("3971c3c470774db8bb9db8892b92c175")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
