package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.millenniumbcp;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MillenniumBcpAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-millenniumbcp-oauth2")
                        .setFinancialInstitutionId("millenniumbcp")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
