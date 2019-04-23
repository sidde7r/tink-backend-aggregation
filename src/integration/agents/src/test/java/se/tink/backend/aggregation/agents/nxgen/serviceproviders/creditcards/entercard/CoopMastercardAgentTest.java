package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

@Ignore
public class CoopMastercardAgentTest {

    private enum Arg {
        USERNAME
    }

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "coopmastercard-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
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
