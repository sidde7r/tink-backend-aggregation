package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class IcaBankenAgentTest {
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
                new AgentIntegrationTest.Builder("se", "se-icabanken-oauth2")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        SSN,
    }
}
