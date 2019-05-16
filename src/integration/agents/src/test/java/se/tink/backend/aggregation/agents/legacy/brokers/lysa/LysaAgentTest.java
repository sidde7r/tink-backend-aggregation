package se.tink.backend.aggregation.agents.brokers.lysa;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class LysaAgentTest {

    private enum Arg {
        USERNAME,
    }

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "lysa")
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
