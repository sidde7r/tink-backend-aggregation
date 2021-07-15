package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.mbank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MbankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pl", "pl-mbank-ob")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("a7ffa204d1dd4e3aadbf3886419f6689")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
