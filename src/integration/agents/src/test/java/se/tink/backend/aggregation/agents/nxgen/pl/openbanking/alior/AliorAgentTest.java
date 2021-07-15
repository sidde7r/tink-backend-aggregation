package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.alior;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AliorAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-alior-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("7278f9dbfae244c1a478e7d1801165a3")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
