package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class DanskebankV31FIAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fi", "fi-danskebank-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("danskebank")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
