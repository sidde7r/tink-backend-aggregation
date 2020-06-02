package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.centreest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CreditAgricoleCentreestAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-creditagricolecentreest-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("creditagricolecentreest")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
