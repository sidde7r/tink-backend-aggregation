package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.bnpparibas.agenttest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BnpParibasAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-bnpparibas-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("97d06fede8c7400e842017df63274c51")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
