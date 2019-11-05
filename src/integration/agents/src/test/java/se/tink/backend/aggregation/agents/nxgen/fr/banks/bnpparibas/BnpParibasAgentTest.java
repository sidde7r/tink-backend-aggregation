package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class BnpParibasAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<BnpParibasAgentTest.Arg> manager =
            new ArgumentManager<>(BnpParibasAgentTest.Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
    }

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fr", "fr-bnpparibas-password")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }
}
