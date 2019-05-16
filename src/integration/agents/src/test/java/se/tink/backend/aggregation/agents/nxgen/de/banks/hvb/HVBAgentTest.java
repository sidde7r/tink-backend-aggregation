package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class HVBAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD,
    }

    private final ArgumentManager<Arg> helper;

    public HVBAgentTest() {
        helper = new ArgumentManager<>(Arg.values());
    }

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-hypovereinsbank-password")
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
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }
}
