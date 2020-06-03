package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BnpParibasAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-bnpparibas-ob")
                        .setFinancialInstitutionId("bnpparibas")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
