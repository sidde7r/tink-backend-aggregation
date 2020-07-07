package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

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
                        .setFinancialInstitutionId("f7dfba4f1a3b4735a64d2de4d6350721");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
