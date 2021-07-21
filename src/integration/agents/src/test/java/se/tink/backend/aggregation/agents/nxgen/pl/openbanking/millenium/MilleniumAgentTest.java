package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.millenium;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MilleniumAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-millenium-ob")
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
