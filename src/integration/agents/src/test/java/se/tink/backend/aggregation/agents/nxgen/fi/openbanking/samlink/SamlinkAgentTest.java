package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SamlinkAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-samlink-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setClusterId("oxford-preprod")
                        .setAppId("tink")
                        .setFinancialInstitutionId("samlink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
