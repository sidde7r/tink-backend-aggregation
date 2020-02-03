package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordnetAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-nordnet-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("nordnet")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
