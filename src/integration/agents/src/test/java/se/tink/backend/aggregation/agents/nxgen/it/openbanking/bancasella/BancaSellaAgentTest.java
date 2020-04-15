package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancasella;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class BancaSellaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-bancasella-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .setFinancialInstitutionId("bancasella")
                        .setAppId("tink")
                        .setClusterId("oxford-preprod");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
