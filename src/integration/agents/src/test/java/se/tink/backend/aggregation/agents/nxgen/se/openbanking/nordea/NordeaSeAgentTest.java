package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class NordeaSeAgentTest {

    private AgentIntegrationTest.Builder builder;

    private enum Arg {
        SSN // 12 digit SSN
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-oauth2")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("nordea")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
