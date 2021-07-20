package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pekao;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class PekaoAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-pekao-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("085d9e292d96438fafe757f3d82c9653")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
