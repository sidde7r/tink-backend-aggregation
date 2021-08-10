package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.millennium;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MillenniumAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-millennium-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("9f18485727644457a9df33724668d75b")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
