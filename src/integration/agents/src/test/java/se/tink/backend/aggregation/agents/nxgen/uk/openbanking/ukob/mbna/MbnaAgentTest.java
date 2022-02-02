package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.mbna;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MbnaAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-mbna-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("32be621f0b3d41bf85f6834e372aeeba")
                .build()
                .testRefresh();
    }
}
