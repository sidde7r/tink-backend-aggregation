package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.CredentialKeys;

public class SparkassenAgentTest {

    private final String TEST_IBAN = "DE90701500001005031263"; // Comma separated list of ibans
    private final String TEST_USERNAME = "1005031263";
    private final String TEST_PASSWORD = "36699";

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-sparkassen-ob")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBANS))
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        IBANS,
        USERNAME,
        PASSWORD
    }
}
