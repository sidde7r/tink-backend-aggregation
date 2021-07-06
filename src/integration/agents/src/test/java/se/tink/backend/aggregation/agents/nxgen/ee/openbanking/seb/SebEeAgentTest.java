package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SebEeAgentTest {

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setUp() {
        builder =
                new AgentIntegrationTest.Builder("ee", "ee-seb-ob")
                        .addCredentialField(Field.Key.USERNAME, "")
                        .addCredentialField(Key.CORPORATE_ID, "")
                        .setFinancialInstitutionId("sebbaltic")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
