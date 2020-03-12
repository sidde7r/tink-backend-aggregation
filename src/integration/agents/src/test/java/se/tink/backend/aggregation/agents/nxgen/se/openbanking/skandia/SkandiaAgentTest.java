package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

// @Ignore
public class SkandiaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-skandiabanken-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("skandiabanken")
                        .setClusterId("oxford-staging")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
