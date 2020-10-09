package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CommerzBankAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-commerzbank-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("commerzbank")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
