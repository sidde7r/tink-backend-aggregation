package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.lanjouetdumaine;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public final class CreditAgricoleLanjouetdumaineAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-creditagricolelanjouetdumaine-ob")
                    .setFinancialInstitutionId("creditagricolelanjouetdumaine")
                    .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
