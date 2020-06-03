package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.savoie.manual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public final class CreditAgricoleSavoieAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-creditagricolesavoie-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("creditagricolesavoie")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
