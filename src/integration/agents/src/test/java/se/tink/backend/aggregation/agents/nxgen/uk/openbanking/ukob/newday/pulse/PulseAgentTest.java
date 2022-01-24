package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.newday.pulse;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class PulseAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-pulse-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("796d3e05b0374cffade5bb1cecc77b14")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
