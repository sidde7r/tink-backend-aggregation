package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class LansforsakringarAgentTest {

    private AgentIntegrationTest.Builder builder;

    private enum Arg {
        SSN // 12 digit SSN
    }

    private final ArgumentManager<LansforsakringarAgentTest.Arg> manager =
            new ArgumentManager<>(LansforsakringarAgentTest.Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-lansforsakringar-oauth2")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
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
