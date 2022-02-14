package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.isp.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class IspAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-isp-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("cbi")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
