package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

@Ignore
public class SantanderEsAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());
    private final String USERNAME = "";
    private final String PASSWORD = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("es", "es-bancosantander-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLogin() throws Exception {
        builder.addCredentialField(Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Key.PASSWORD, helper.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }
}
