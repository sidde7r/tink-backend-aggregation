package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class HVBAgentTest {
    private final ArgumentManager<Arg> helper;
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-hypovereinsbank-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    public HVBAgentTest() {
        helper = new ArgumentManager<>(Arg.values());
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    };;

    @Before
    public void before() {
        helper.before();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .build()
                .testRefresh();
    }

    private enum Arg {
        USERNAME,
        PASSWORD,
    }
}
