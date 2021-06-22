package se.tink.backend.aggregation.agents.nxgen.es.openbanking.openbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class OpenbankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("es", "es-redsys-openbank-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("17def709bf0b4fc9b79c690c6eb615ad")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
