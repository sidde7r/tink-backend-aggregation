package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class DanskebankAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-danskebank-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("danskebank")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
