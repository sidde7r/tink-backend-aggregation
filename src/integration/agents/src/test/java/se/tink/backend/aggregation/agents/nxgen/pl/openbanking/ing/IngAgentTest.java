package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.ing;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class IngAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-ing-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("8c81ab6d0fa9428c8371d9922c9d328b")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
