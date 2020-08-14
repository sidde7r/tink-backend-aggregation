package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.manual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SkandiaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-skandiabanken-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("skandiabanken")
                        .setClusterId("oxford-preprod")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
