package se.tink.backend.aggregation.agents.banks.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class SebApiAgentIntegrationTest {

    private enum Arg {
        USERNAME,
    }

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "seb-bankid")
                    .expectLoggedIn(false)
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(true);
    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .build()
                .testRefresh();
    }
}
