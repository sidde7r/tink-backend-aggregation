package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class NovoBancoAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<NovoBancoAgentTest.Arg> helper = new ArgumentManager<>(NovoBancoAgentTest.Arg.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("pt", "pt-novobanco-password")
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
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(NovoBancoAgentTest.Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(NovoBancoAgentTest.Arg.PASSWORD))
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
