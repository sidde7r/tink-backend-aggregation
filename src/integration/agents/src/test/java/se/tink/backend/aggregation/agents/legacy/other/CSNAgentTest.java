package se.tink.backend.aggregation.agents.other;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class CSNAgentTest {

    /** Leftover credentials from old test class: 198203300382 6110 */
    private enum Arg {
        SSN, // 12 digits
        PASSWORD // 4 digits
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "csn")
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
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.SSN))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }
}
