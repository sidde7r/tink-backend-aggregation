package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pko;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class PkoAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-pkobp-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("5a5d8ca117e4404ead3649eee3f620e2")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
